package com.aibe.team2.domain.resume.dto;

public record ResumeAnalysisEvent(
        Long reportId,
        String resumeContent,
        String fullJobDescription
) {}