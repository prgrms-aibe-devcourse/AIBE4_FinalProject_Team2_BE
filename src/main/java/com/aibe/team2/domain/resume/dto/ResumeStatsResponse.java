package com.aibe.team2.domain.resume.dto;

public record ResumeStatsResponse(
        long aiResumeCount,
        long savedResumeCount,
        long completedCount
) {
}
