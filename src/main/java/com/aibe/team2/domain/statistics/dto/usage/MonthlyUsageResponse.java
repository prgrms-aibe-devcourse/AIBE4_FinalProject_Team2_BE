package com.aibe.team2.domain.statistics.dto.usage;

public record MonthlyUsageResponse(
        int year,
        int month,
        long resumeAnalysisCount,
        long interviewCount
) { }
