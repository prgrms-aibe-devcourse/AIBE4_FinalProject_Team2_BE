package com.aibe.team2.domain.interview.controller;

import com.aibe.team2.domain.interview.dto.InterviewStartRequest;
import com.aibe.team2.domain.interview.dto.UserAnswerRequest;
import com.aibe.team2.domain.interview.dto.VoiceSessionResponse;
import com.aibe.team2.domain.interview.entity.InterviewSession;
import com.aibe.team2.domain.interview.service.ConversationManager;
import com.aibe.team2.domain.interview.service.InterviewManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
public class InterviewController {

    private final ConversationManager conversationManager;
    private final InterviewManager interviewManager;

    @PostMapping("/start")
    public ResponseEntity<InterviewSession> startInterview(@RequestBody InterviewStartRequest request) {
        InterviewSession session = interviewManager.startInterview(
                request.getMemberId(),
                request.getResumeId(),
                request.getJobPostingId(),
                request.getInterviewMode(),
                request.getInterviewType(),
                request.getAiProvider()
        );
        return ResponseEntity.ok(session);
    }

    // 1. 텍스트 면접: 사용자의 답변을 받고 AI의 꼬리 질문을 SSE로 스트리밍
    @GetMapping(value = "/{sessionId}/text/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamTextInterview(
            @PathVariable Long sessionId,
            @RequestParam String answer) {
        SseEmitter emitter = new SseEmitter(120000L); // 2분 타임아웃

        conversationManager.startTextStreaming(sessionId, answer, emitter);

        return emitter;
    }

    // 2. 음성 면접: Retell AI 세션을 시작하고 토큰 반환
    @PostMapping("/{sessionId}/voice/start")
    public VoiceSessionResponse startVoiceInterview(@PathVariable Long sessionId) {
        return conversationManager.startVoiceInterview(sessionId);
    }
}