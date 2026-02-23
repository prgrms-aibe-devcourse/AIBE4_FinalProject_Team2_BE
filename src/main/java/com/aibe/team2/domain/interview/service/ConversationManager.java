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

    // [Real Mode] OpenAI 스트리밍 연결
    public void startTextStreaming(String answer, SseEmitter emitter) {
        openAiService.streamQuestion(answer).subscribe(
                data -> {
                    try {
                        // OpenAI의 응답 데이터(JSON)를 그대로 클라이언트에 전송
                        // (프론트엔드에서 파싱 필요, 또는 여기서 파싱해서 content만 보낼 수도 있음)
                        emitter.send(SseEmitter.event().name("message").data(data));
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                },
                error -> emitter.completeWithError(error),
                emitter::complete
        );
    }

    // [Real Mode] Retell AI 세션 생성
    public VoiceSessionResponse startVoiceInterview(Long sessionId) {
        return retellService.createVoiceCall(sessionId);
    }
}