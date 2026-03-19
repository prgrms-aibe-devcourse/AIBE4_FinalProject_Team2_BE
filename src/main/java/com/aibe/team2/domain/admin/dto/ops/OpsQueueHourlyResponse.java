package com.aibe.team2.domain.admin.dto.ops;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OpsQueueHourlyResponse {
    private String hour;
    private long enqueuedCount;
    private long successCount;
    private long failedCount;
}