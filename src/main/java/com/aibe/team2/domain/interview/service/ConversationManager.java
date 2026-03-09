package com.aibe.team2.domain.interview.service;

import com.aibe.team2.domain.interview.dto.InterviewRequestDto;
import com.aibe.team2.domain.interview.dto.VoiceSessionResponse;
import com.aibe.team2.domain.interview.enums.InterviewMode;
import com.aibe.team2.domain.interview.enums.InterviewSessionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ConversationManager {

    private final GeminiService geminiService;
    private final RetellService retellService;
    private final InterviewManager interviewManager; // 추가: 상태 변경을 위한 Manager 주입

    // @DistributedLock(key = "text-streaming", waitTime = 1, leaseTime = 20) <- 추후 분산 락 구현시 주석 해제
    public void startTextStreaming(Long sessionId, String answer, String modelVariant, InterviewMode interviewMode, SseEmitter emitter) {
        InterviewRequestDto request = new InterviewRequestDto(answer, modelVariant, interviewMode);

        geminiService.streamQuestion(String.valueOf(sessionId), request).subscribe(
                data -> {
                    try {
                        emitter.send(SseEmitter.event().name("message").data(data));
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                },
                error -> {
                    // 추가: 스트리밍 중 에러 발생 시 세션을 ABORTED 상태로 전환
                    interviewManager.advanceStatus(sessionId, InterviewSessionStatus.ABORTED);
                    emitter.completeWithError(error);
                },
                emitter::complete
        );
    }

    public VoiceSessionResponse startVoiceInterview(Long sessionId) {
        return retellService.createVoiceCall(sessionId);
    }
}