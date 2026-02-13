package com.aibe.team2.domain.interview.controller;

import com.aibe.team2.domain.interview.dto.UserAnswerRequest;
import com.aibe.team2.domain.interview.entity.InterviewSession;
import com.aibe.team2.domain.interview.entity.InterviewStatus;
import com.aibe.team2.domain.interview.service.InterviewManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.bind.annotation.*;
import java.util.concurrent.Executors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/interviews")
public class InterviewController {

    private final InterviewManager interviewManager;

    @GetMapping(value = "/start-test", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter startInterviewWithDb() {
        // 면접 세션 생성
        InterviewSession session = interviewManager.startInterview("TEXT");

        SseEmitter emitter = new SseEmitter(120 * 1000L);
        String fullSentence = "반갑습니다. 면접 세션(" + session.getId() + ")이 생성되었습니다.";
        String[] characters = fullSentence.split("");

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // 상태 전이: READY
                interviewManager.advanceStatus(session.getId(), InterviewStatus.READY);

                for (String character : characters) {
                    emitter.send(SseEmitter.event().data(character));
                    Thread.sleep(100);
                }

                // 상태 전이: ANSWERING
                interviewManager.advanceStatus(session.getId(), InterviewStatus.ANSWERING);
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    @PostMapping("/sessions/{sessionId}/answer")
    public ResponseEntity<Void> receiveAnswer(
            @PathVariable Long sessionId,
            @RequestBody UserAnswerRequest request // 텍스트 또는 음성 파일 경로
    ) {
        // 사용자가 대답을 완료하면 이 API가 호출됨
        // 대화 매니저가 AI 질문 생성을 시작함
        conversationManager.processAnswerAndGenerateNextQuestion(sessionId, request.getContent());
        return ResponseEntity.ok().build();
    }
}