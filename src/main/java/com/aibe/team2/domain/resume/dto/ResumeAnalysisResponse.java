package com.aibe.team2.domain.resume.dto;

import com.aibe.team2.domain.resume.entity.ResumeAnalysisReport;
import com.aibe.team2.global.common.constant.AnalysisStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ResumeAnalysisResponse(
        Long id,
        Long resumeId,
        Long jobPostingId,
        Integer matchScore,

        @Schema(description = "AI가 생성한 소제목 (JSON 형식의 String)", example = "{\"title\": \"...\", \"reason\": \"...\"}")
        String generatedSubtitle,

        @Schema(description = "키워드 분석 결과 (JSON 형식의 String)")
        String keywordAnalysis,

        @Schema(description = "문장 교정 데이터 (JSON 형식의 String)")
        String sentenceCorrection,
        // 추후 json 형식의 String은 FE에서 json.parse() 할 것.

        String revisedFullContent,
        AnalysisStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ResumeAnalysisResponse from(ResumeAnalysisReport report) {
        return new ResumeAnalysisResponse(
                report.getId(),
                report.getResume().getId(),
                report.getJobPostingId(),
                report.getMatchScore(),
                report.getGeneratedSubtitle(),
                report.getKeywordAnalysis(),
                report.getSentenceCorrection(),
                report.getRevisedFullContent(),
                report.getStatus(),
                report.getCreatedAt(),
                report.getUpdatedAt()
        );
    }
}