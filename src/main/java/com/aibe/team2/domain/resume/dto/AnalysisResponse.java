package com.aibe.team2.domain.resume.dto;

import com.aibe.team2.domain.resume.entity.AnalysisStatus;
import com.aibe.team2.domain.resume.entity.AnalysisType;
import com.aibe.team2.domain.resume.entity.AnalyzedReport;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AnalysisResponse(
        Long reportId,
        Long resumeId,
        Long jobPostingId,            // 일반 첨삭일 경우 null
        AnalysisType analysisType,    // NORMAL or FIT_MATCH
        AnalysisStatus status,

        // [공통 영역]
        String overallFeedback,
        String sentenceCorrections,           // JSON String (문장별 교정 내역)
        String revisedFullContent,

        // [매칭 전용 영역] (일반 첨삭일 경우 null)
        Integer matchScore,
        String matchingFeedback,
        String keywordAnalysis,       // JSON String (키워드 분석)

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AnalysisResponse from(AnalyzedReport report) {
        return AnalysisResponse.builder()
                .reportId(report.getId())
                .resumeId(report.getResume().getId())
                // JobPosting이 있으면 ID를, 없으면 null을 반환
                .jobPostingId(report.getJobPosting() != null ? report.getJobPosting().getId() : null)
                .analysisType(report.getAnalysisType())
                .status(report.getStatus())

                // 공통 영역
                .overallFeedback(report.getOverallFeedback())
                .sentenceCorrections(report.getSentenceCorrections())
                .revisedFullContent(report.getRevisedFullContent())

                // 매칭 전용 영역
                .matchScore(report.getMatchScore())
                .matchingFeedback(report.getMatchingFeedback())
                .keywordAnalysis(report.getKeywordAnalysis())

                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }
}