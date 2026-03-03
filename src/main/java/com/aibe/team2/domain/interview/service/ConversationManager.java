package com.aibe.team2.domain.interview.service;

import com.aibe.team2.domain.interview.dto.InterviewRequestDto;
import com.aibe.team2.domain.interview.dto.VoiceSessionResponse;
//import com.aibe.team2.global.lock.DistributedLock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ConversationManager {

    private final GeminiService geminiService;
    private final RetellService retellService;

    //@DistributedLock(key = "text-streaming", waitTime = 1, leaseTime = 20)
    public void startTextStreaming(Long sessionId, String answer, String modelVariant, String personaType, SseEmitter emitter) {
        InterviewRequestDto request = new InterviewRequestDto(answer, modelVariant, personaType);

        geminiService.streamQuestion(String.valueOf(sessionId), request).subscribe(
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

    public VoiceSessionResponse startVoiceInterview(Long sessionId) {
        return retellService.createVoiceCall(sessionId);
    }
}