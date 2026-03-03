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
    public InterviewSession startInterview(Long memberId, Long resumeId, Long jobPostingId, String mode, String type, String aiProvider, String modelVariant, String personaType) {
        InterviewSession session = InterviewSession.builder()
                .memberId(memberId)
                .resumeId(resumeId)
                .jobPostingId(jobPostingId)
                .interviewMode(mode)
                .interviewType(type)
                .aiProvider(aiProvider)
                .modelVariant(modelVariant) // 🚀 엔티티 필드에 추가 (엔티티에 필드가 선언되어 있어야 함)
                .personaType(personaType)   // 🚀 엔티티 필드에 추가
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