package com.aibe.team2.domain.statistics.repository.usage;

import com.aibe.team2.domain.statistics.dto.admin.UsageLogAdminRow;
import com.aibe.team2.domain.statistics.dto.admin.UsageLogAdminSearchCond;
import com.aibe.team2.domain.statistics.dto.usage.MonthlyUsageStatResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UsageLogRepositoryCustom {

    List<MonthlyUsageStatResponse> findMonthlyStats(Long memberId, int year);

    Page<UsageLogAdminRow> searchAdminUsageLogs(
            UsageLogAdminSearchCond cond,
            Pageable pageable
    );

    List<UsageLogAdminRow> findTop5AdminUsageLogs();
}