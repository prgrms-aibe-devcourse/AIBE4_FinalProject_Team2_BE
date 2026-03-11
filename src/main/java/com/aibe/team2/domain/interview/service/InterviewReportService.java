package com.aibe.team2.domain.interview.service;

import com.aibe.team2.domain.interview.dto.InterviewReportResponse;
import com.aibe.team2.domain.interview.entity.InterviewSession;
import com.aibe.team2.domain.interview.enums.InterviewSessionStatus;
import com.aibe.team2.domain.jobposting.entity.JobPosting;
import com.aibe.team2.domain.jobposting.repository.JobPostingRepository;
import com.aibe.team2.domain.resume.entity.Resume;
import com.aibe.team2.domain.resume.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterviewReportService {

    private final ResumeRepository resumeRepository;
    private final JobPostingRepository jobPostingRepository;

    public InterviewReportResponse getReport(InterviewSession session) {

        // 🚀 [FR-INT-08] 핵심: DONE 상태 세션만 리포트 조회가 가능해야 한다.
        if (session.getStatus() != InterviewSessionStatus.DONE) {
            throw new IllegalStateException("면접이 정상적으로 완료된(DONE) 세션만 리포트를 조회할 수 있습니다. 현재 상태: " + session.getStatus());
        }

        // 이력서 제목 조회
        String resumeTitle = null;
        if (session.getResumeId() != null) {
            resumeTitle = resumeRepository.findByIdAndMemberId(session.getResumeId(), session.getMemberId())
                    .map(Resume::getTitle)
                    .orElse("삭제된 이력서");
        }
        // 채용 공고 제목 조회
        String jobTitle = null;
        if (session.getJobPostingId() != null) {
            jobTitle = jobPostingRepository.findByIdAndMemberId(session.getJobPostingId(), session.getMemberId())
                    .map(JobPosting::getJobTitle)
                    .orElse("삭제된 공고");
        }

        return InterviewReportResponse.of(session, resumeTitle, jobTitle);
    }
}