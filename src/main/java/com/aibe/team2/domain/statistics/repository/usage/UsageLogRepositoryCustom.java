package com.aibe.team2.domain.statistics.repository.usage;

import com.aibe.team2.domain.statistics.dto.usage.MonthlyUsageStatDto;
import java.util.List;

public interface UsageLogRepositoryCustom {

    List<MonthlyUsageStatDto> findMonthlyStats(Long memberId, int year);
}