package com.aibe.team2.domain.interview.service;

import com.aibe.team2.domain.interview.entity.InterviewSession;
import com.aibe.team2.domain.interview.enums.ExperienceLevel;
import com.aibe.team2.domain.interview.enums.InterviewMode;
import com.aibe.team2.domain.interview.enums.InterviewSessionStatus;
import com.aibe.team2.domain.interview.enums.JobRole;
import com.aibe.team2.domain.interview.repository.InterviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewManager {

    private final InterviewRepository interviewRepository;
    private final InterviewAnalysisService interviewAnalysisService;

    @Transactional
    // @DistributedLock(key = "interview-start", waitTime = 1, leaseTime = 3)
    // [수정] 파라미터에 jobRole, experience 추가
    public InterviewSession startInterview(Long memberId, Long resumeId,
                                           Long jobPostingId, String jobDescription,
                                           InterviewMode interviewMode, String interviewType,
                                           String aiProvider, String modelVariant,
                                           String jobRole, String experience) {
        InterviewSession session = InterviewSession.builder()
                .memberId(memberId)
                .resumeId(resumeId)
                .jobPostingId(jobPostingId)
                .jobDescription(jobDescription)
                .interviewMode(interviewMode)
                .interviewType(interviewType)
                .aiProvider(aiProvider)
                .modelVariant(modelVariant)
                .jobRole(JobRole.from(jobRole))
                .experience(ExperienceLevel.from(experience))
                .build();

        return interviewRepository.save(session);
    }

    // @Transactional 어노테이션 제거: 상태 변경 내역이 DB에 즉시 반영(Commit)되도록 함
    public void advanceStatus(Long sessionId, InterviewSessionStatus nextStatus) {
        InterviewSession session = interviewRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("세션을 찾을 수 없습니다. ID: " + sessionId));

        session.updateStatus(nextStatus);

        // JpaRepository의 save는 자체 트랜잭션을 가지므로 이 순간 DB에 즉시 커밋
        interviewRepository.save(session);

        // DB 커밋이 완료된 후 안전하게 비동기 분석 호출
        if (nextStatus == InterviewSessionStatus.DONE && "TEXT".equalsIgnoreCase(session.getInterviewType())) {
            log.info("텍스트 면접 종료 감지 - AI 분석 자동 트리거. SessionID: {}", sessionId);
            interviewAnalysisService.analyzeSession(sessionId);
        }
    }
}