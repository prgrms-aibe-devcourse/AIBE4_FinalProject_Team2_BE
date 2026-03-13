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
        Long jobPostingId,
        AnalysisType analysisType,
        AnalysisStatus status,

        // [공통 영역]
        String overallFeedback,
        String sentenceCorrections,


        String paragraphSummaries,

        String revisedFullContent,

        // [매칭 전용 영역]
        Integer matchScore,
        String matchingFeedback,
        String keywordAnalysis,
        String expectedQuestions,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static AnalysisResponse from(AnalyzedReport report) {
        return AnalysisResponse.builder()
                .reportId(report.getId())
                .resumeId(report.getResume().getId())
                .jobPostingId(report.getJobPosting() != null ? report.getJobPosting().getId() : null)
                .analysisType(report.getAnalysisType())
                .status(report.getStatus())
                .overallFeedback(report.getOverallFeedback())
                .sentenceCorrections(report.getSentenceCorrections())
                .paragraphSummaries(report.getParagraphSummaries())
                .revisedFullContent(report.getRevisedFullContent())
                .matchScore(report.getMatchScore())
                .matchingFeedback(report.getMatchingFeedback())
                .keywordAnalysis(report.getKeywordAnalysis())
                .expectedQuestions(report.getExpectedQuestions())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .build();
    }
}