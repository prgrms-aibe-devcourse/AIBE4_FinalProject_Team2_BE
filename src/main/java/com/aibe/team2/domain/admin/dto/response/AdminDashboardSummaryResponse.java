package com.aibe.team2.domain.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminDashboardSummaryResponse {

    private Long activeMemberCount;
    private Long dormancyMemberCount;
    private Long deletedMemberCount;

    private Long todayUsageLogCount;
    private Long todayResumeUsageCount;
    private Long todayInterviewUsageCount;

    private Long todayTotalTokenUsage;
}