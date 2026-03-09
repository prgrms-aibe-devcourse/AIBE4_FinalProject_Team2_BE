package com.aibe.team2.domain.statistics.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminServiceUsageSummaryResponse {
    private Long resumeUsage;
    private Long interviewUsage;
    private Long adminOperations;
    private Long totalUsage;
}