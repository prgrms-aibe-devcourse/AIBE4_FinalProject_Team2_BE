package com.aibe.team2.domain.interview.service;

import com.aibe.team2.domain.interview.entity.InterviewSession;
import com.aibe.team2.domain.interview.entity.InterviewSessionStatus;
import com.aibe.team2.domain.interview.repository.InterviewRepository;
import com.aibe.team2.global.redis.lock.DistributedLock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InterviewManager {

    private final InterviewRepository interviewRepository;

    // 락 키는 "interview-start:memberId:resumeId:..." 형태로 동적 생성됩니다.
    @DistributedLock(key = "interview-start", waitTime = 1, leaseTime = 3)
    @Transactional
    public InterviewSession startInterview(Long memberId, Long resumeId, Long jobPostingId, String interviewMode, String interviewType, String aiProvider) {
        InterviewSession session = InterviewSession.builder()
                .memberId(memberId)
                .resumeId(resumeId)
                .jobPostingId(jobPostingId)
                .interviewMode(interviewMode)
                .interviewType(interviewType)
                .aiProvider(aiProvider)
                .build();

        return interviewRepository.save(session);
    }


    @Transactional
    public void advanceStatus(Long sessionId, InterviewSessionStatus nextStatus) {
        InterviewSession session = interviewRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다. ID: " + sessionId));

        // CREATED -> IN_PROGRESS -> DONE/ABORTED 흐름을 반영한 상태 업데이트
        session.updateStatus(nextStatus);
    }
}