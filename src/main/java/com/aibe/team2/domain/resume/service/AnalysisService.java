package com.aibe.team2.domain.resume.service;

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
                .analysisType(AnalysisType.NORMAL)
                .jobPosting(null)
                .jobDescriptionText(null) // 🚀 일반 첨삭은 공고가 없으므로 null 전달
                .build();

        resumeAnalysisRepository.save(report);

        eventPublisher.publishEvent(
                AnalysisEvent.first(report.getId(), resume.getContent())
        );

        return report.getId();
    }

    // 2. 채용 공고 기반 매칭 및 첨삭 요청 (FIT_MATCH)
    @Transactional
    public Long requestMatchAnalysis(Long resumeId, Long memberId, String jobDescriptionText) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESUME_NOT_FOUND));

        if (!resume.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.COMMON_403);
        }

        // 🚀 더 이상 DB에서 JobPosting을 조회하지 않습니다. 파라미터로 받은 텍스트를 그대로 저장합니다.
        AnalyzedReport report = AnalyzedReport.builder()
                .resume(resume)
                .analysisType(AnalysisType.FIT_MATCH)
                .jobPosting(null) // 기존 DB 조회 연관관계는 null
                .jobDescriptionText(jobDescriptionText) // 🔥 프론트에서 받은 텍스트 직접 삽입
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