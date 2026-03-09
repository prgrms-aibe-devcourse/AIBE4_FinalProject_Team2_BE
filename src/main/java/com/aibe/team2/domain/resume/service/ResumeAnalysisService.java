package com.aibe.team2.domain.resume.service;

import com.aibe.team2.domain.jobposting.entity.JobPosting;
import com.aibe.team2.domain.jobposting.entity.JobSkill;
import com.aibe.team2.domain.jobposting.repository.JobPostingRepository;
import com.aibe.team2.domain.resume.dto.AnalysisEvent;
import com.aibe.team2.domain.resume.entity.AnalyzedReport;
import com.aibe.team2.domain.resume.entity.Resume;
import com.aibe.team2.domain.resume.repository.ResumeAnalysisRepository;
import com.aibe.team2.domain.resume.repository.ResumeRepository;
import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.BusinessException;
import com.aibe.team2.global.redis.lock.DistributedLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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

@DistributedLock(key = "'resume-analysis-' + #resumeId", waitTime = 1, leaseTime = 5)
    @Transactional
    public Long analyzeResume(Long resumeId, Long jobPostingId, Long memberId) {

        // 1. 자기소개서 조회 및 권한 검증
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESUME_NOT_FOUND));

        if (!resume.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.COMMON_403);
        }

        // 2. 채용 공고 조회
        JobPosting jobPosting = jobPostingRepository.findById(jobPostingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.JOB_POSTING_NOT_FOUND));

        if (!jobPosting.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.COMMON_403);
        }

        String jobSkillsText = jobPosting.getJobSkills().stream()
                .map(JobSkill::getSkillName)
                .collect(Collectors.joining(", "));

        String fullJobDescription = jobPosting.getJobDescription();
        if (!jobSkillsText.isEmpty()) {
            fullJobDescription += "\n\n[요구 기술 스택]\n" + jobSkillsText;
        }

        Optional<AnalyzedReport> existingReport = resumeAnalysisRepository
                .findTopByResumeIdOrderByCreatedAtDesc(resumeId);

        AnalyzedReport report;
        if (existingReport.isPresent() && existingReport.get().getJobPosting().getId().equals(jobPostingId)) {
            report = existingReport.get();
            report.startAnalysis();
        } else {
            report = AnalyzedReport.builder()
                    .resume(resume)
                    .jobPosting(jobPosting)
                    .build();
            report.startAnalysis();
        }

        // 1. 상태를 PROCESSING으로 DB에 먼저 확정(Save) 합니다.
        AnalyzedReport savedReport = resumeAnalysisRepository.save(report);

        // Redis Queue에 직접 넣지 않고, 이벤트를 발행합니다.
        // 이 트랜잭션이 무사히 Commit 된 이후에 리스너가 동작하게 됩니다.
        eventPublisher.publishEvent(new AnalysisEvent(
                savedReport.getId(),
                resume.getContent(),
                fullJobDescription
        ));

        return savedReport.getId();
    }

    @Transactional(readOnly = true)
    public AnalyzedReport getAnalysisResult(Long resumeId, Long memberId) {

        // 1. 이력서 조회 및 내 이력서가 맞는지 보안 검증
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESUME_NOT_FOUND));

        if (!resume.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.COMMON_403);
        }

        // 2. 검증 통과 시에만 결과 반환
        return resumeAnalysisRepository.findTopByResumeIdOrderByCreatedAtDesc(resumeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ANALYSIS_REPORT_NOT_FOUND));
    }
}