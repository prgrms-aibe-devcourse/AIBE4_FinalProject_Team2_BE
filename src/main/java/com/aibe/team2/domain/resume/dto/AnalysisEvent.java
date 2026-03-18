package com.aibe.team2.domain.resume.dto;

public record AnalysisEvent(
        Long reportId,
        String resumeContent,
        Integer retryCount
) {
    public static AnalysisEvent first(Long reportId, String resumeContent) {
        return new AnalysisEvent(reportId, resumeContent, 0);
    }

    public static AnalysisEvent retry(Long reportId, String resumeContent, Integer retryCount) {
        return new AnalysisEvent(reportId, resumeContent, retryCount);
    }
}