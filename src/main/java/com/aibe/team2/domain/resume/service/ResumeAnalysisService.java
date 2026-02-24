package com.aibe.team2.domain.resume.service;

import com.aibe.team2.domain.jobposting.entity.JobPosting;
import com.aibe.team2.domain.jobposting.repository.JobPostingRepository;
import com.aibe.team2.domain.resume.entity.Resume;
import com.aibe.team2.domain.resume.entity.ResumeAnalysisReport;
import com.aibe.team2.domain.resume.repository.ResumeAnalysisRepository;
import com.aibe.team2.domain.resume.repository.ResumeRepository;
import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.custom.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeAnalysisService {

    private final ResumeRepository resumeRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final JobPostingRepository jobPostingRepository;

    // [추가] 방금 만든 비동기 워커를 주입받습니다.
    private final ResumeAnalysisAsyncWorker asyncWorker;

    @Transactional
    public Long analyzeResume(Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RESUME_NOT_FOUND));

        Long defaultJobPostingId = 1L;
        JobPosting jobPosting = jobPostingRepository.findById(defaultJobPostingId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COMMON_404));

        Optional<ResumeAnalysisReport> existingReport = resumeAnalysisRepository
                .findTopByResumeIdOrderByCreatedAtDesc(resumeId);

        ResumeAnalysisReport report;
        if (existingReport.isPresent() && existingReport.get().getJobPostingId().getId().equals(defaultJobPostingId)) {
            report = existingReport.get();
            report.startAnalysis(); // PROCESSING 상태로 변경
        } else {
            report = ResumeAnalysisReport.builder()
                    .resume(resume)
                    .jobPostingId(jobPosting)
                    .build();
            report.startAnalysis();
        }

        // 1. 상태를 PROCESSING으로 DB에 먼저 확정(Save) 합니다.
        ResumeAnalysisReport savedReport = resumeAnalysisRepository.save(report);

        // 2. [핵심] 톰캣 스레드는 여기서 워커에게 일을 던져놓고 기다리지 않습니다!
        asyncWorker.processAiAnalysisAsync(savedReport.getId(), resume.getContent(), jobPosting.getJobDescription());

        // 3. 사용자(프론트엔드)에게는 즉시 리포트 ID를 반환하여 로딩 화면을 보여주게 합니다.
        return savedReport.getId();
    }

    @Transactional(readOnly = true)
    public ResumeAnalysisReport getAnalysisResult(Long resumeId) {
        return resumeAnalysisRepository.findTopByResumeIdOrderByCreatedAtDesc(resumeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COMMON_404));
    }
}