package com.aibe.team2.domain.interview.service;

import com.aibe.team2.domain.interview.dto.VoiceSessionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ConversationManager {

    private final OpenAiService openAiService;
    private final RetellService retellService;

    // 텍스트 면접: 무조건 OpenAI 스트리밍 사용
    public void startTextStreaming(String answer, SseEmitter emitter) {
        openAiService.streamQuestion(answer).subscribe(
                data -> {
                    try {
                        emitter.send(SseEmitter.event().name("message").data(data));
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                },
                error -> emitter.completeWithError(error),
                emitter::complete
        );
    }

    // 음성 면접: 무조건 Retell 세션 생성 사용
    public VoiceSessionResponse startVoiceInterview(Long sessionId) {
        return retellService.createVoiceCall(sessionId);
    }
}