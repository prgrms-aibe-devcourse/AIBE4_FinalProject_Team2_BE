package com.aibe.team2.domain.resume.service;

import com.aibe.team2.domain.jobposting.entity.JobPosting;
import com.aibe.team2.domain.jobposting.entity.JobSkill;
import com.aibe.team2.domain.jobposting.repository.JobPostingRepository;
import com.aibe.team2.domain.resume.dto.ResumeAnalysisEvent; // ✅ 이벤트 import
import com.aibe.team2.domain.resume.entity.Resume;
import com.aibe.team2.domain.resume.entity.ResumeAnalysisReport;
import com.aibe.team2.domain.resume.repository.ResumeAnalysisRepository;
import com.aibe.team2.domain.resume.repository.ResumeRepository;
import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher; // ✅ Publisher import
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeAnalysisService {

    private final ResumeRepository resumeRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final JobPostingRepository jobPostingRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Long analyzeResume(Long resumeId, Long jobPostingId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESUME_NOT_FOUND));

        JobPosting jobPosting = jobPostingRepository.findById(jobPostingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.JOB_POSTING_NOT_FOUND));

        String jobSkillsText = jobPosting.getJobSkills().stream()
                .map(JobSkill::getSkillName)
                .collect(Collectors.joining(", "));

        String fullJobDescription = jobPosting.getJobDescription();
        if (!jobSkillsText.isEmpty()) {
            fullJobDescription += "\n\n[요구 기술 스택]\n" + jobSkillsText;
        }

        Optional<ResumeAnalysisReport> existingReport = resumeAnalysisRepository
                .findTopByResumeIdOrderByCreatedAtDesc(resumeId);

        ResumeAnalysisReport report;
        if (existingReport.isPresent() && existingReport.get().getJobPostingId().getId().equals(jobPostingId)) {
            report = existingReport.get();
            report.startAnalysis();
        } else {
            report = ResumeAnalysisReport.builder()
                    .resume(resume)
                    .jobPostingId(jobPosting)
                    .build();
            report.startAnalysis();
        }

        // 1. 상태를 PROCESSING으로 DB에 먼저 확정(Save) 합니다.
        ResumeAnalysisReport savedReport = resumeAnalysisRepository.save(report);

        // Redis Queue에 직접 넣지 않고, 이벤트를 발행합니다.
        // 이 트랜잭션이 무사히 Commit 된 이후에 리스너가 동작하게 됩니다.
        eventPublisher.publishEvent(new ResumeAnalysisEvent(
                savedReport.getId(),
                resume.getContent(),
                fullJobDescription
        ));

        return savedReport.getId();
    }

    @Transactional(readOnly = true)
    public ResumeAnalysisReport getAnalysisResult(Long resumeId) {
        return resumeAnalysisRepository.findTopByResumeIdOrderByCreatedAtDesc(resumeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ANALYSIS_REPORT_NOT_FOUND));
    }
}