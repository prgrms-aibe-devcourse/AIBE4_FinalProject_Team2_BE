package com.aibe.team2.domain.resume.service;

import com.aibe.team2.domain.jobposting.entity.JobPosting;
import com.aibe.team2.domain.jobposting.repository.JobPostingRepository;
import com.aibe.team2.domain.resume.dto.AnalysisEvent;
import com.aibe.team2.domain.resume.dto.AnalysisResponse;
import com.aibe.team2.domain.resume.entity.AnalysisStatus;
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
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final ApplicationEventPublisher eventPublisher;

    // 분석 재시도
    @Transactional
    public void retryAnalysis(Long reportId, Long memberId) {
        AnalyzedReport report = resumeAnalysisRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_404));

        // 권한 체크: 본인의 자소서에 대한 리포트인지 확인
        if (!report.getResume().getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.COMMON_403);
        }

        // 상태 체크: FAILED 혹은 DELAYED 상태일 때만 재시도 허용
        if (report.getStatus() != AnalysisStatus.FAILED && report.getStatus() != AnalysisStatus.DELAYED) {
            throw new BusinessException(ErrorCode.COMMON_400);
        }
        report.updateStatus(AnalysisStatus.PENDING);

        // 다시 워커 큐(이벤트)로 발행
        eventPublisher.publishEvent(
                AnalysisEvent.retry(
                        report.getId(),
                        report.getResume().getContent(),
                        1
                )
        );
    }

    @Transactional
    public Long requestNormalAnalysis(Long resumeId, Long memberId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESUME_NOT_FOUND));

        if (!resume.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.COMMON_403);
        }

        AnalyzedReport report = AnalyzedReport.builder()
                .resume(resume)
                .analysisType(AnalysisType.FIT_MATCH)
                .jobPosting(null)
                .build();

        resumeAnalysisRepository.save(report);

        // ★ 워커 직접 호출 대신 이벤트를 발행하여 Queue Producer에게 넘김
        eventPublisher.publishEvent(
                AnalysisEvent.first(report.getId(), resume.getContent())
        );

        return report.getId();
    }

    @Transactional
    public Long requestMatchAnalysis(Long resumeId, Long memberId, Long jobPostingId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESUME_NOT_FOUND));

        if (!resume.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.COMMON_403);
        }

        JobPosting jobPosting = jobPostingRepository.findById(jobPostingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.JOB_POSTING_NOT_FOUND));

        AnalyzedReport report = AnalyzedReport.builder()
                .resume(resume)
                .analysisType(AnalysisType.FIT_MATCH)
                .jobPosting(jobPosting)
                .build();
        resumeAnalysisRepository.save(report);

        eventPublisher.publishEvent(
                AnalysisEvent.first(report.getId(), resume.getContent())
        );

        return report.getId();
    }

    @Transactional(readOnly = true)
    public AnalysisResponse getAnalysisResult(Long resumeId, Long reportId, Long memberId) {

        Resume resume = resumeRepository.findByIdAndMemberId(resumeId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESUME_NOT_FOUND));

        AnalyzedReport report = resumeAnalysisRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_404));

        if (!report.getResume().getId().equals(resumeId)) {
            throw new BusinessException(ErrorCode.COMMON_403);
        }

        return AnalysisResponse.builder()
                .reportId(report.getId())
                .resumeId(resume.getId())
                .jobPostingId(report.getJobPosting() != null ? report.getJobPosting().getId() : null)
                .analysisType(report.getAnalysisType())
                .status(report.getStatus())
                // --- 매칭 분석 전용 데이터 ---
                .matchScore(report.getMatchScore())
                .matchingFeedback(report.getMatchingFeedback())

                .keywordAnalysis(report.getKeywordAnalysis())
                .expectedQuestions(report.getExpectedQuestions())
                // --- 공통 분석 데이터 ---
                .overallFeedback(report.getOverallFeedback())
                .sentenceCorrections(report.getSentenceCorrections())
                .paragraphSummaries(report.getParagraphSummaries())
                .revisedFullContent(report.getRevisedFullContent())
                .createdAt(report.getCreatedAt())
                .build();
    }
}