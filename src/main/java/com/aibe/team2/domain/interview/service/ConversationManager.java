package com.aibe.team2.domain.interview.service;

import com.aibe.team2.domain.interview.dto.InterviewRequestDto;
import com.aibe.team2.domain.interview.dto.VoiceSessionResponse;
import com.aibe.team2.domain.interview.entity.InterviewSession;
import com.aibe.team2.domain.interview.enums.InterviewMode;
import com.aibe.team2.domain.interview.enums.InterviewSessionStatus;
import com.aibe.team2.domain.resume.entity.Resume;
import com.aibe.team2.domain.resume.repository.ResumeRepository;
import com.aibe.team2.global.redis.lock.DistributedLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationManager {

    private final GeminiService geminiService;
    private final RetellService retellService;
    private final InterviewManager interviewManager; // 추가: 상태 변경을 위한 Manager 주입
    private final ResumeRepository resumeRepository; //  [FR-INT-06] 이력서 조회를 위한 의존성 추가

    @DistributedLock(key = "text-streaming", waitTime = 1, leaseTime = 20)
    public void startTextStreaming(InterviewSession session, String answer, String modelVariant, InterviewMode interviewMode, SseEmitter emitter) {

        // [FR-INT-06] 자기소개서 내용 안전하게 조회 및 추출
        String resumeContent = null;
        if (session.getResumeId() != null) {
            resumeContent = resumeRepository.findByIdAndMemberId(session.getResumeId(), session.getMemberId())
                    .map(Resume::getContent)
                    .orElse(null); // 권한이 없거나 찾을 수 없으면 주입하지 않음
        }

        // DTO 생성 시 추출한 이력서 데이터 포함
        InterviewRequestDto request = new InterviewRequestDto(answer, modelVariant, interviewMode, resumeContent);

        geminiService.streamQuestion(String.valueOf(session.getId()), request).subscribe(
                data -> {
                    try {
                        emitter.send(SseEmitter.event().name("message").data(data));
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                },
                error -> {
                    log.error("스트리밍 중 에러 발생. 세션을 ABORTED 상태로 전환합니다. SessionID: {}", session.getId(), error);
                    // 통계 제외를 위해 에러 발생 시 상태를 ABORTED로 변경
                    interviewManager.advanceStatus(session.getId(), InterviewSessionStatus.ABORTED);
                    emitter.completeWithError(error);
                },
                emitter::complete
        );
    }

    public VoiceSessionResponse startVoiceInterview(Long sessionId) {
        return retellService.createVoiceCall(sessionId);
    }
}