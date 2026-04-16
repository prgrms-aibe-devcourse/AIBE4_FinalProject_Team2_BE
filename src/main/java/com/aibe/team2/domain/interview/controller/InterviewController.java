package com.aibe.team2.domain.interview.controller;

import com.aibe.team2.domain.interview.dto.InterviewReportResponse;
import com.aibe.team2.domain.interview.dto.InterviewStartRequest;
import com.aibe.team2.domain.interview.dto.VoiceSessionResponse;
import com.aibe.team2.domain.interview.entity.InterviewSession;
import com.aibe.team2.domain.interview.enums.InterviewMode;
import com.aibe.team2.domain.interview.enums.InterviewSessionStatus;
import com.aibe.team2.domain.interview.repository.InterviewRepository;
import com.aibe.team2.domain.interview.service.ConversationManager;
import com.aibe.team2.domain.interview.service.InterviewManager;
import com.aibe.team2.domain.interview.service.InterviewReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import com.aibe.team2.domain.auth.dto.CustomUserDetails;

@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
public class InterviewController {

    private final ConversationManager conversationManager;
    private final InterviewManager interviewManager;
    private final InterviewRepository interviewRepository;
    private final InterviewReportService interviewReportService;

    @PostMapping("/start")
    public ResponseEntity<InterviewSession> startInterview(
            @RequestBody InterviewStartRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long authenticatedMemberId = (userDetails != null) ? getMemberIdFromUserDetails(userDetails) : request.getMemberId();

            // [수정] jobRole과 experience 파라미터 추가
            InterviewSession session = interviewManager.startInterview(
                    authenticatedMemberId,
                    request.getResumeId(),
                    request.getJobPostingId(),
                    request.getJobDescription(),
                    InterviewMode.from(request.getInterviewMode()),
                    request.getInterviewType(),
                    request.getAiProvider(),
                    request.getModelVariant(),
                    request.getJobRole(),         // 추가됨
                    request.getExperience()  // 추가됨
            );
            return ResponseEntity.ok(session);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping(value = "/{sessionId}/text/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamTextInterview(
            @PathVariable Long sessionId,
            @RequestParam String answer,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false, defaultValue = "gemini-flash-latest") String modelVariant,
            @RequestParam(required = false, defaultValue = "NORMAL") String interviewMode) {

        InterviewSession session = validateAndGetSessionOwnership(sessionId, userDetails, memberId);

        if (session.getStatus() == InterviewSessionStatus.CREATED) {
            interviewManager.advanceStatus(sessionId, InterviewSessionStatus.IN_PROGRESS);
        }

        SseEmitter emitter = new SseEmitter(120000L);
        try {
            conversationManager.startTextStreaming(session, answer, modelVariant, InterviewMode.from(interviewMode), emitter);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return emitter;
    }

    @PostMapping("/{sessionId}/voice/start")
    public VoiceSessionResponse startVoiceInterview(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long memberId) {

        InterviewSession session = validateAndGetSessionOwnership(sessionId, userDetails, memberId);

        if (session.getStatus() == InterviewSessionStatus.CREATED) {
            interviewManager.advanceStatus(sessionId, InterviewSessionStatus.IN_PROGRESS);
        }

        return conversationManager.startVoiceInterview(sessionId);
    }

    @PatchMapping("/{sessionId}/end")
    public ResponseEntity<Void> endInterview(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long memberId) {

        validateAndGetSessionOwnership(sessionId, userDetails, memberId);

        interviewManager.advanceStatus(sessionId, InterviewSessionStatus.DONE);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{sessionId}/report")
    public ResponseEntity<InterviewReportResponse> getInterviewReport(
            @PathVariable Long sessionId,
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long memberId) {

        InterviewSession session = validateAndGetSessionOwnership(sessionId, userDetails, memberId);

        try {
            InterviewReportResponse report = interviewReportService.getReport(session);
            return ResponseEntity.ok(report);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
        }
    }

    //보안 리뷰 반영 및 SSE 우회: Security 인증 객체와 fallbackMemberId를 유연하게 처리
    private InterviewSession validateAndGetSessionOwnership(Long sessionId, UserDetails userDetails, Long fallbackMemberId) {
        Long authenticatedMemberId;

        // 1. Security Context에 정보가 있으면 우선 사용
        if (userDetails != null) {
            authenticatedMemberId = getMemberIdFromUserDetails(userDetails);
        }
        // 2. EventSource(SSE) 헤더 누락 및 로컬 테스트 환경을 위해 파라미터 값으로 우회(Fallback) 허용
        else if (fallbackMemberId != null) {
            authenticatedMemberId = fallbackMemberId;
        }
        // 3. 둘 다 없으면 401 에러
        else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요한 서비스입니다.");
        }

        InterviewSession session = interviewRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "면접 세션을 찾을 수 없습니다."));

        if (!session.getMemberId().equals(authenticatedMemberId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 면접 세션에 접근할 권한이 없습니다.");
        }

        return session;
    }

    // UserDetails에서 MemberId 추출을 돕는 유틸 메서드
    private Long getMemberIdFromUserDetails(UserDetails userDetails) {
        // 1. CustomUserDetails로 형변환하여 실제 Member 엔티티의 ID(PK)를 안전하게 가져옴
        if (userDetails instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getMember().getMemberId();
        }

        // 2. 예외 상황에 대한 폴백(Fallback) 처리
        try {
            return Long.valueOf(userDetails.getUsername());
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "회원 식별자 형식이 올바르지 않습니다.");
        }
    }
}