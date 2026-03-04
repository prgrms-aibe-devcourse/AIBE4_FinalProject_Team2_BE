package com.aibe.team2.domain.interview.controller;

import com.aibe.team2.domain.interview.dto.InterviewStartRequest;
import com.aibe.team2.domain.interview.dto.VoiceSessionResponse;
import com.aibe.team2.domain.interview.entity.InterviewSession;
import com.aibe.team2.domain.interview.enums.InterviewMode;
import com.aibe.team2.domain.interview.repository.InterviewRepository;
import com.aibe.team2.domain.interview.service.ConversationManager;
import com.aibe.team2.domain.interview.service.InterviewManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/interviews")
@RequiredArgsConstructor
public class InterviewController {

    private final ConversationManager conversationManager;
    private final InterviewManager interviewManager;
    private final InterviewRepository interviewRepository;

    @PostMapping("/start")
    public ResponseEntity<InterviewSession> startInterview(@RequestBody InterviewStartRequest request) {
        try {
            InterviewSession session = interviewManager.startInterview(
                    request.getMemberId(),
                    request.getResumeId(),
                    request.getJobPostingId(),
                    InterviewMode.fromString(request.getInterviewMode()),
                    request.getInterviewType(),
                    request.getAiProvider(),
                    request.getModelVariant()
            );
            return ResponseEntity.ok(session);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping(value = "/{sessionId}/text/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamTextInterview(
            @PathVariable Long sessionId,
            @RequestParam String answer,
            @RequestParam Long memberId,
            @RequestParam(required = false, defaultValue = "gemini-flash-latest") String modelVariant,
            @RequestParam(required = false, defaultValue = "NORMAL") String interviewMode) {

        InterviewSession session = interviewRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "면접 세션을 찾을 수 없습니다."));

        if (!session.getMemberId().equals(memberId)) {
            // 다른 사용자의 세션에 접근하려는 경우 403 Forbidden 반환
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 면접 세션에 대한 접근 권한이 없습니다.");
        }

        SseEmitter emitter = new SseEmitter(120000L);
        conversationManager.startTextStreaming(sessionId, answer, modelVariant, InterviewMode.fromString(interviewMode), emitter);

        return emitter;
    }

    @PostMapping("/{sessionId}/voice/start")
    public VoiceSessionResponse startVoiceInterview(
            @PathVariable Long sessionId,
            @RequestParam Long memberId) { //음성 면접 시작 시에도 소유권 확인 추가

        InterviewSession session = interviewRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "면접 세션을 찾을 수 없습니다."));

        if (!session.getMemberId().equals(memberId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 면접 세션에 대한 접근 권한이 없습니다.");
        }

        return conversationManager.startVoiceInterview(sessionId);
    }
}