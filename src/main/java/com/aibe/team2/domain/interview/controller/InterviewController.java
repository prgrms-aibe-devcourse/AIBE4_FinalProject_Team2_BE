package com.aibe.team2.domain.interview.controller;

import com.aibe.team2.domain.interview.dto.InterviewStartRequest;
import com.aibe.team2.domain.interview.dto.VoiceSessionResponse;
import com.aibe.team2.domain.interview.entity.InterviewSession;
import com.aibe.team2.domain.interview.enums.InterviewMode;
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
                InterviewMode.valueOf(request.getInterviewMode()),
                request.getInterviewType(),
                request.getAiProvider(),
                request.getModelVariant()
        );
        return ResponseEntity.ok(session);
    }

    @GetMapping(value = "/{sessionId}/text/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamTextInterview(
            @PathVariable Long sessionId,
            @RequestParam String answer,
            @RequestParam(required = false, defaultValue = "gemini-flash-latest") String modelVariant,
            @RequestParam(required = false, defaultValue = "NORMAL") String interviewMode) {

        SseEmitter emitter = new SseEmitter(120000L);

        conversationManager.startTextStreaming(
                sessionId,
                answer,
                modelVariant,
                InterviewMode.valueOf(interviewMode),
                emitter
        );

        return emitter;
    }

    @PostMapping("/{sessionId}/voice/start")
    public VoiceSessionResponse startVoiceInterview(@PathVariable Long sessionId) {
        return conversationManager.startVoiceInterview(sessionId);
    }
}