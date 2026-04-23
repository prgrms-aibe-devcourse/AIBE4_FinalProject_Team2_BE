package com.aibe.team2.domain.admin.service;

import com.aibe.team2.domain.admin.dto.response.AdminDashboardSummaryResponse;
import com.aibe.team2.domain.mypage.entity.enums.MemberStatus;
import com.aibe.team2.domain.mypage.repository.member.MemberRepository;
import com.aibe.team2.domain.statistics.dto.admin.UsageLogAdminRow;
import com.aibe.team2.domain.statistics.enums.ServiceType;
import com.aibe.team2.domain.statistics.repository.usage.UsageLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final MemberRepository memberRepository;
    private final UsageLogRepository usageLogRepository;

    public AdminDashboardSummaryResponse getSummary() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        Long activeMemberCount = memberRepository.countByStatus(MemberStatus.ACTIVE);
        Long dormancyMemberCount = memberRepository.countByStatus(MemberStatus.DORMANCY;
        Long deletedMemberCount = memberRepository.countByStatus(MemberStatus.DELETED);

        Long todayUsageLogCount = usageLogRepository.countByCreatedAtBetween(start, end);
        Long todayResumeUsageCount = usageLogRepository.countByServiceTypeAndCreatedAtBetween(ServiceType.RESUME, start, end);
        Long todayInterviewUsageCount = usageLogRepository.countByServiceTypeAndCreatedAtBetween(ServiceType.INTERVIEW, start, end);

        Long todayTotalTokenUsage = usageLogRepository.sumTokenUsageByCreatedAtBetween(start, end);
        if (todayTotalTokenUsage == null) {
            todayTotalTokenUsage = 0L;
        }

        return new AdminDashboardSummaryResponse(
                activeMemberCount,
                dormancyMemberCount,
                deletedMemberCount,
                todayUsageLogCount,
                todayResumeUsageCount,
                todayInterviewUsageCount,
                todayTotalTokenUsage
        );
    }

    public List<UsageLogAdminRow> getRecentLogs() {
        return usageLogRepository.findTop5AdminUsageLogs();
    }
}