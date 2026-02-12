package com.aibe.team2.domain.interview.service;

import com.aibe.team2.domain.interview.entity.InterviewSession;
import com.aibe.team2.domain.interview.entity.InterviewStatus;
import com.aibe.team2.domain.interview.repository.InterviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InterviewManager {

    private final InterviewRepository interviewRepository;

    @Transactional
    public InterviewSession startInterview(String type) {
        InterviewSession session = InterviewSession.builder()
                .type(type)
                .build();
        return interviewRepository.save(session);
    }

    @Transactional
    public void advanceStatus(Long sessionId, InterviewStatus nextStatus) {
        InterviewSession session = interviewRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다."));
        session.updateStatus(nextStatus);
    }
}