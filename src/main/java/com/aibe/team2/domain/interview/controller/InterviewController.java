package com.aibe.team2.domain.interview.controller;

import com.aibe.team2.domain.interview.dto.InterviewStartRequest;
import com.aibe.team2.domain.interview.dto.VoiceSessionResponse;
import com.aibe.team2.domain.interview.entity.InterviewSession;
import com.aibe.team2.domain.interview.enums.InterviewMode;
import com.aibe.team2.domain.interview.enums.InterviewSessionStatus;
import com.aibe.team2.domain.interview.repository.InterviewRepository;
import com.aibe.team2.domain.interview.service.ConversationManager;
import com.aibe.team2.domain.interview.service.InterviewManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
public class InterviewController {

    private final ConversationManager conversationManager;
    private final InterviewManager interviewManager;
    private final InterviewRepository interviewRepository; // 세션 조회를 위해 Repository 의존성 추가

    @PostMapping("/start")
    public ResponseEntity<InterviewSession> startInterview(@RequestBody InterviewStartRequest request) {
        try {
            InterviewSession session = interviewManager.startInterview(
                    request.getMemberId(),
                    request.getResumeId(),
                    request.getJobPostingId(),
                    InterviewMode.from(request.getInterviewMode()),
                    request.getInterviewType(),
                    request.getAiProvider(),
                    request.getModelVariant()
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
            @RequestParam Long memberId, // 프론트엔드 연동용 로그인 유저 ID
            @RequestParam(required = false, defaultValue = "gemini-flash-latest") String modelVariant,
            @RequestParam(required = false, defaultValue = "NORMAL") String interviewMode) {

        // 리뷰 반영: 전용 private 메서드로 조회 및 권한 검증 위임 (코드 중복 제거 및 보안 강화)
        InterviewSession session = validateAndGetSessionOwnership(sessionId, memberId);

        // [FR-INT-02] 상태 전이 로직
        if (session.getStatus() == InterviewSessionStatus.CREATED) {
            interviewManager.advanceStatus(sessionId, InterviewSessionStatus.IN_PROGRESS);
        }

        SseEmitter emitter = new SseEmitter(120000L);

        try {
            conversationManager.startTextStreaming(
                    sessionId,
                    answer,
                    modelVariant,
                    InterviewMode.from(interviewMode),
                    emitter
            );
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        return emitter;
    }

    @PostMapping("/{sessionId}/voice/start")
    public VoiceSessionResponse startVoiceInterview(
            @PathVariable Long sessionId,
            @RequestParam Long memberId) {

        // 두 번째 리뷰 반영: startVoiceInterview 엔드포인트 역시 동일한 검증 위임
        InterviewSession session = validateAndGetSessionOwnership(sessionId, memberId);

        if (session.getStatus() == InterviewSessionStatus.CREATED) {
            interviewManager.advanceStatus(sessionId, InterviewSessionStatus.IN_PROGRESS);
        }

        return conversationManager.startVoiceInterview(sessionId);
    }

    @PatchMapping("/{sessionId}/end")
    public ResponseEntity<Void> endInterview(
            @PathVariable Long sessionId,
            @RequestParam Long memberId) {

        // 세 번째 리뷰 반영: 면접 종료 엔드포인트 역시 동일한 검증 위임
        validateAndGetSessionOwnership(sessionId, memberId);

        interviewManager.advanceStatus(sessionId, InterviewSessionStatus.DONE);
        return ResponseEntity.ok().build();
    }

    // 보안 리뷰 및 팀원 피드백 반영: IDOR 취약점 방지 및 중복 로직 제거를 위한 전용 검증 메서드 (streamTextInterview, startVoiceInterview, endInterview 모든 엔드포인트에 공통 적용됨)
     // 현재는 프론트엔드 연동을 위해 @RequestParam 으로 넘겨받은 memberId를 검증에 사용 중
     // 추후 Spring Security 적용 시, 컨트롤러 파라미터를 삭제하고 @AuthenticationPrincipal SecurityContext에서 추출한 실제 로그인 사용자의 ID로 변경하여 완벽한 인가 처리를 수행
    private InterviewSession validateAndGetSessionOwnership(Long sessionId, Long authenticatedMemberId) {
        InterviewSession session = interviewRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "면접 세션을 찾을 수 없습니다."));

        if (!session.getMemberId().equals(authenticatedMemberId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 면접 세션에 접근할 권한이 없습니다.");
        }

        return session;
    }
}