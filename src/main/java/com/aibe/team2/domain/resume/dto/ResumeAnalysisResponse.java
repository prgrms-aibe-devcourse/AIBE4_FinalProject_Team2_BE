package com.aibe.team2.domain.resume.dto;

import com.aibe.team2.domain.resume.entity.ResumeAnalysisReport;
import com.aibe.team2.global.common.constant.AnalysisStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Map;

public record ResumeAnalysisResponse(
        Long id,
        Long resumeId,
        Long jobPostingId,
        Integer matchScore,

        // String -> Map<String, Object>
        // JSON 형식의 String -> JSON 객체
        @Schema(description = "AI가 생성한 소제목 (JSON 객체)", example = "{\"title\": \"...\", \"reason\": \"...\"}")
        Map<String, Object> generatedSubtitle,

        @Schema(description = "키워드 분석 결과 (JSON 객체)")
        Map<String, Object> keywordAnalysis,

        @Schema(description = "문장 교정 데이터 (JSON 객체)")
        Map<String, Object> sentenceCorrection,
        // 추후 json 형식의 String은 FE에서 json.parse() 할 것. -> Spring Boot(Jackson)가 자동으로 JSON 변환을 해주므로 FE에서 json.parse() 할 필요없음

        String revisedFullContent,
        AnalysisStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ResumeAnalysisResponse from(ResumeAnalysisReport report) {
        return new ResumeAnalysisResponse(
                report.getId(),
                report.getResume().getId(),
                // [수정]
                report.getJobPostingId().getId(),
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