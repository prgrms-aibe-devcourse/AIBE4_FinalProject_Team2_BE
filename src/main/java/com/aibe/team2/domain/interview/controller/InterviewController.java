package com.aibe.team2.domain.interview.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController
@RequestMapping("/api/interviews")
public class InterviewController {

    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect() {
        // SSE 연결 객체 생성 (유효시간 30분)
        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);

        try {
            // 연결 직후 데이터를 보내야 연결 유지가 됨
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("실시간 면접 세션에 연결되었습니다."));
        } catch (IOException e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }
}