package com.aibe.team2.domain.admin.dto.ops;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OpsDashboardSummaryResponse {
    private long todayErrorCount;
    private long todayQueueEnqueuedCount;
    private long todayQueueSuccessCount;
    private long todayQueueFailedCount;
    private double todayQueueSuccessRate;
    private long unresolvedIssueCount;
}