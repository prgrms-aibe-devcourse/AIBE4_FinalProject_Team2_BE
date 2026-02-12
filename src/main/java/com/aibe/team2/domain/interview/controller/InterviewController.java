package com.aibe.team2.domain.interview.controller;

import com.aibe.team2.domain.interview.entity.InterviewSession;
import com.aibe.team2.domain.interview.entity.InterviewStatus;
import com.aibe.team2.domain.interview.service.InterviewManager;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
}