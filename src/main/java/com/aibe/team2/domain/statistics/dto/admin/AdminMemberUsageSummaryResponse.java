package com.aibe.team2.domain.statistics.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminMemberUsageSummaryResponse {

    private Long memberId;
    private String email;
    private Integer creditBalance;

    private Long totalLogCount;
    private Long totalTokenUsage;

    private Long resumeUsageCount;
    private Long interviewUsageCount;
}