package com.aibe.team2.domain.interview.service;

import com.aibe.team2.domain.interview.dto.VoiceSessionResponse;
import com.aibe.team2.global.redis.lock.DistributedLock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ConversationManager {

    private final GeminiService geminiService;
    private final RetellService retellService;

    // 락 키 예시: "interview-answer:15" (세션 ID 15번에 대해 락이 걸림)
    @DistributedLock(key = "text-streaming", waitTime = 1, leaseTime = 20)
    public void startTextStreaming(String answer, SseEmitter emitter) {
        geminiService.streamQuestion(answer).subscribe(
                data -> {
                    try {
                        emitter.send(SseEmitter.event().name("message").data(data));
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                },
                emitter::completeWithError,
                emitter::complete
        );
    }

    // 음성 면접: Retell 세션 생성 사용
    @DistributedLock(key = "voice-interview", waitTime = 1, leaseTime = 20)
    public VoiceSessionResponse startVoiceInterview(Long sessionId) {
        return retellService.createVoiceCall(sessionId);
    }
}