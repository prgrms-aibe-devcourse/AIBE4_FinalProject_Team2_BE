package com.aibe.team2.domain.interview.service;

import com.aibe.team2.domain.interview.entity.InterviewSession;
import com.aibe.team2.domain.interview.entity.InterviewSessionStatus;
import com.aibe.team2.domain.interview.repository.InterviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InterviewManager {

    private final InterviewRepository interviewRepository;

    @Transactional
    public InterviewSession startInterview(Long memberId, Long resumeId, Long jobPostingId, String mode, String type, String aiProvider) {
        InterviewSession session = InterviewSession.builder()
                .memberId(memberId)      // ERD: member_id
                .resumeId(resumeId)      // ERD: resume_id
                .jobPostingId(jobPostingId) // ERD: job_posting_id
                .interviewMode(mode)     // ERD: interview_mode
                .interviewType(type)     // ERD: interview_type
                .aiProvider(aiProvider) // 사용자 선택 반영
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