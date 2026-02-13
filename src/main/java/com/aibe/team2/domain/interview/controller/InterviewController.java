package com.aibe.team2.domain.interview.controller;

import com.aibe.team2.domain.interview.dto.UserAnswerRequest;
import com.aibe.team2.domain.interview.dto.VoiceSessionResponse;
import com.aibe.team2.domain.interview.service.ConversationManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
public class InterviewController {

    private final ConversationManager conversationManager;

    // 1. 텍스트 면접: 사용자의 답변을 받고 AI의 꼬리 질문을 SSE로 스트리밍
    @GetMapping(value = "/{sessionId}/text/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamTextInterview(
            @PathVariable Long sessionId,
            @RequestParam String answer) {
        SseEmitter emitter = new SseEmitter(120000L); // 2분 타임아웃
        conversationManager.startTextStreaming(answer, emitter);
        return emitter;
    }

    // 2. 음성 면접: Retell AI 세션을 시작하고 토큰 반환
    @PostMapping("/{sessionId}/voice/start")
    public VoiceSessionResponse startVoiceInterview(@PathVariable Long sessionId) {
        return conversationManager.startVoiceInterview(sessionId);
    }

    // 서버 정상 작동 확인용
    @GetMapping("/check")
    public String checkServer() {
        return "Server is Running! (Interview Lead: Wonjun)";
    }
}