package com.aibe.team2.domain.resume.service;

import com.aibe.team2.domain.jobposting.entity.JobPosting;
import com.aibe.team2.domain.jobposting.repository.JobPostingRepository;
import com.aibe.team2.domain.resume.dto.AnalysisEvent;
import com.aibe.team2.domain.resume.entity.AnalysisType;
import com.aibe.team2.domain.resume.entity.AnalyzedReport;
import com.aibe.team2.domain.resume.entity.Resume;
import com.aibe.team2.domain.resume.repository.ResumeAnalysisRepository;
import com.aibe.team2.domain.resume.repository.ResumeRepository;
import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final ResumeRepository resumeRepository;
    private final JobPostingRepository jobPostingRepository;
    private final ResumeAnalysisRepository analysisRepository;
    private final ApplicationEventPublisher eventPublisher; // ★ 이벤트 발행기 추가

    @Transactional
    public Long requestNormalAnalysis(Long resumeId, Long memberId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESUME_NOT_FOUND));

        if (!resume.getMemberId().equals(memberId)) throw new BusinessException(ErrorCode.COMMON_403);

        AnalyzedReport report = AnalyzedReport.builder()
                .resume(resume)
                .analysisType(AnalysisType.NORMAL)
                .jobPosting(null)
                .build();
        analysisRepository.save(report);

        // ★ 워커 직접 호출 대신 이벤트를 발행하여 Queue Producer에게 넘김
        eventPublisher.publishEvent(new AnalysisEvent(report.getId(), resume.getContent()));

        return report.getId();
    }

    @Transactional
    public Long requestMatchAnalysis(Long resumeId, Long memberId, Long jobPostingId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESUME_NOT_FOUND));

        if (!resume.getMemberId().equals(memberId)) throw new BusinessException(ErrorCode.COMMON_403);

        JobPosting jobPosting = jobPostingRepository.findById(jobPostingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.JOB_POSTING_NOT_FOUND));

        AnalyzedReport report = AnalyzedReport.builder()
                .resume(resume)
                .analysisType(AnalysisType.FIT_MATCH)
                .jobPosting(jobPosting)
                .build();
        analysisRepository.save(report);

        // ★ 워커 직접 호출 대신 이벤트를 발행하여 Queue Producer에게 넘김
        eventPublisher.publishEvent(new AnalysisEvent(report.getId(), resume.getContent()));

        return report.getId();
    }
}