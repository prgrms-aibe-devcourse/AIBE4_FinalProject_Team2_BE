package com.aibe.team2.domain.admin.dto.ops;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OpsQueueSummaryResponse {
    private long enqueuedCount;
    private long processingCount;
    private long successCount;
    private long failedCount;
    private long cancelledCount;
    private double successRate;
}