package com.aibe.team2.domain.statistics.dto.admin;

import com.aibe.team2.domain.statistics.enums.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DailyUsageAdminRow {
    private ServiceType serviceType;
    private Long totalCount;
    private Long totalTokenUsage;
    private Long logCount;
}