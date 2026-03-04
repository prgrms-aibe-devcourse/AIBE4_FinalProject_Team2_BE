package com.aibe.team2.domain.interview.service;

import com.aibe.team2.domain.interview.entity.InterviewSession;
import com.aibe.team2.domain.interview.enums.InterviewMode;
import com.aibe.team2.domain.interview.enums.InterviewSessionStatus;
import com.aibe.team2.domain.interview.repository.InterviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InterviewManager {

    private final InterviewRepository interviewRepository;

    @Transactional
    // @DistributedLock(key = "interview-start", waitTime = 1, leaseTime = 3)
    public InterviewSession startInterview(Long memberId, Long resumeId, Long jobPostingId,
                                           InterviewMode interviewMode, String interviewType,
                                           String aiProvider, String modelVariant) {
        InterviewSession session = InterviewSession.builder()
                .memberId(memberId)
                .resumeId(resumeId)
                .jobPostingId(jobPostingId)
                .interviewMode(interviewMode)
                .interviewType(interviewType)
                .aiProvider(aiProvider)
                .modelVariant(modelVariant)
                .build();

        return interviewRepository.save(session);
    }

    @Transactional
    public void advanceStatus(Long sessionId, InterviewSessionStatus nextStatus) {
        InterviewSession session = interviewRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다. ID: " + sessionId));
        session.updateStatus(nextStatus);
    }
}