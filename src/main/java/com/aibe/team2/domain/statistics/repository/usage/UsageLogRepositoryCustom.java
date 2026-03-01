package com.aibe.team2.domain.statistics.repository.usage;

import com.aibe.team2.domain.statistics.dto.usage.MonthlyUsageStatResponse;
import java.util.List;

public interface UsageLogRepositoryCustom {

    List<MonthlyUsageStatResponse> findMonthlyStats(Long memberId, int year);
}