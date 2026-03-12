package com.aibe.team2.domain.resume.service;

import com.aibe.team2.domain.jobposting.entity.JobPosting;
import com.aibe.team2.domain.jobposting.repository.JobPostingRepository;
import com.aibe.team2.domain.resume.entity.AnalysisType;
import com.aibe.team2.domain.resume.entity.AnalyzedReport;
import com.aibe.team2.domain.resume.entity.Resume;
import com.aibe.team2.domain.resume.repository.ResumeAnalysisRepository;
import com.aibe.team2.domain.resume.repository.ResumeRepository;
import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final ResumeRepository resumeRepository;
    private final JobPostingRepository jobPostingRepository;
    private final ResumeAnalysisRepository analysisRepository;
    private final AnalysisAsyncWorker analysisAsyncWorker;

    // 1. 일반 첨삭 요청 로직
    @Transactional
    public Long requestNormalAnalysis(Long resumeId, Long memberId) {
        // 이력서 조회 및 소유권 검증
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESUME_NOT_FOUND));

        if (!resume.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.COMMON_403); // 본인 글만 분석 가능
        }

        // 일반 첨삭용 리포트 생성 (JobPosting 은 null)
        AnalyzedReport report = AnalyzedReport.builder()
                .resume(resume)
                .analysisType(AnalysisType.NORMAL)
                .jobPosting(null)
                .build();

        analysisRepository.save(report);

        // 비동기 AI 분석 워커 호출
        analysisAsyncWorker.processAiAnalysisAsync(report.getId(), resume.getContent());

        return report.getId();
    }

    // 2. 채용 공고 매칭 요청 로직
    @Transactional
    public Long requestMatchAnalysis(Long resumeId, Long memberId, Long jobPostingId) {
        // 이력서 조회 및 소유권 검증
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESUME_NOT_FOUND));

        if (!resume.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.COMMON_403);
        }

        // 채용 공고 조회
        JobPosting jobPosting = jobPostingRepository.findById(jobPostingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.JOB_POSTING_NOT_FOUND));

        // 매칭 분석용 리포트 생성 (JobPosting 연관관계 맺기)
        AnalyzedReport report = AnalyzedReport.builder()
                .resume(resume)
                .analysisType(AnalysisType.FIT_MATCH)
                .jobPosting(jobPosting)
                .build();

        analysisRepository.save(report);

        // 비동기 AI 분석 워커 호출
        analysisAsyncWorker.processAiAnalysisAsync(report.getId(), resume.getContent());

        return report.getId();
    }
    // 3. 첨삭 요청한 자기소개서 조회
    @Transactional(readOnly = true)
    public Long getAnalysisReport(Long resumeId, Long memberId) {
        // 이력서 조회 및 소유권 검증
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESUME_NOT_FOUND));
        if (!resume.getMemberId().equals(memberId))
            throw new BusinessException(ErrorCode.COMMON_403);

        // 최신 분석 결과 조회
        AnalyzedReport report = analysisRepository.findTopByResumeIdOrderByCreatedAtDesc(resumeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ANALYSIS_REPORT_NOT_FOUND));

        return report.getId();

    }
}