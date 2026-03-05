package com.aibe.team2.domain.statistics.service;

import com.aibe.team2.domain.statistics.dto.admin.DailyUsageAdminRow;
import com.aibe.team2.domain.statistics.dto.admin.UsageLogAdminRow;
import com.aibe.team2.domain.statistics.dto.admin.UsageLogAdminSearchCond;
import com.aibe.team2.domain.statistics.repository.usage.UsageLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUsageService {

    private final UsageLogRepository usageLogRepository;

    public List<DailyUsageAdminRow> getDailyUsage(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        return usageLogRepository.aggregateDailyAdmin(start, end);
    }

    public Page<UsageLogAdminRow> searchUsageLogs(
            UsageLogAdminSearchCond cond,
            Pageable pageable
    ) {
        return usageLogRepository.searchAdminUsageLogs(cond, pageable);
    }
}