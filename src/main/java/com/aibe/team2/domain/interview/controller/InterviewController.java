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
    private final InterviewRepository interviewRepository; // 세션 조회 및 상태 확인용 리포지토리 의존성 추가

    @PostMapping("/start")
    public ResponseEntity<InterviewSession> startInterview(@RequestBody InterviewStartRequest request) {
        try {
            // 안전한 from() 메서드 사용
            InterviewSession session = interviewManager.startInterview(
                    request.getMemberId(),
                    request.getResumeId(),
                    request.getJobPostingId(),
                    InterviewMode.from(request.getInterviewMode()),
                    request.getInterviewType(),
                    request.getAiProvider(),
                    request.getModelVariant()
            );
            return ResponseEntity.ok(session); // 초기 생성 시 상태는 내부적으로 CREATED
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping(value = "/{sessionId}/text/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamTextInterview(
            @PathVariable Long sessionId,
            @RequestParam String answer,
            @RequestParam Long memberId, // 프론트엔드에서 넘긴 memberId 받기
            @RequestParam(required = false, defaultValue = "gemini-flash-latest") String modelVariant,
            @RequestParam(required = false, defaultValue = "NORMAL") String interviewMode) {

        // 1. 세션 존재 여부 확인
        InterviewSession session = interviewRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "면접 세션을 찾을 수 없습니다."));

        // 2. 보안: 소유권 검증 (IDOR 방어) - 다른 사람의 세션 스트리밍 훔쳐보기 방지
        if (!session.getMemberId().equals(memberId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 면접 세션에 접근할 권한이 없습니다.");
        }

        // 3. [FR-INT-02] 상태 전이: 생성된 세션이 처음 호출될 때 IN_PROGRESS 로 변경
        if (session.getStatus() == InterviewSessionStatus.CREATED) {
            interviewManager.advanceStatus(sessionId, InterviewSessionStatus.IN_PROGRESS);
        }

        SseEmitter emitter = new SseEmitter(120000L);

        try {
            conversationManager.startTextStreaming(
                    sessionId,
                    answer,
                    modelVariant,
                    InterviewMode.from(interviewMode), // 안전한 파싱
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
            @RequestParam Long memberId) { // 프론트엔드 연동용 memberId

        InterviewSession session = interviewRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "면접 세션을 찾을 수 없습니다."));

        // 보안: 음성 통화 역시 소유권 철저히 검증
        if (!session.getMemberId().equals(memberId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 면접 세션에 접근할 권한이 없습니다.");
        }

        // [FR-INT-02] 상태 전이: 음성 세션 시작 시 IN_PROGRESS 로 변경
        if (session.getStatus() == InterviewSessionStatus.CREATED) {
            interviewManager.advanceStatus(sessionId, InterviewSessionStatus.IN_PROGRESS);
        }

        return conversationManager.startVoiceInterview(sessionId);
    }

    @PatchMapping("/{sessionId}/end")
    public ResponseEntity<Void> endInterview(
            @PathVariable Long sessionId,
            @RequestParam Long memberId) {

        InterviewSession session = interviewRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "면접 세션을 찾을 수 없습니다."));

        if (!session.getMemberId().equals(memberId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 면접 세션에 접근할 권한이 없습니다.");
        }

        // 상태 전이: 면접 종료 시 DONE 으로 변경
        interviewManager.advanceStatus(sessionId, InterviewSessionStatus.DONE);
        return ResponseEntity.ok().build();
    }
}