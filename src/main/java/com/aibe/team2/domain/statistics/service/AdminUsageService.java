package com.aibe.team2.domain.statistics.service;

import com.aibe.team2.domain.mypage.entity.Member;
import com.aibe.team2.domain.mypage.repository.member.MemberRepository;
import com.aibe.team2.domain.statistics.dto.admin.*;
import com.aibe.team2.domain.statistics.enums.ServiceType;
import com.aibe.team2.domain.statistics.repository.usage.UsageLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminUsageService {

    private final UsageLogRepository usageLogRepository;

    public List<DailyUsageAdminRow> getDailyUsage(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        List<DailyUsageAdminRow> rows =
                usageLogRepository.aggregateDailyAdmin(start, end);

        Map<ServiceType, DailyUsageAdminRow> rowMap = rows.stream()
                .collect(Collectors.toMap(DailyUsageAdminRow::getServiceType, r -> r));

        return Arrays.stream(ServiceType.values())
                .map(type -> rowMap.getOrDefault(
                        type,
                        new DailyUsageAdminRow(type, 0L, 0L, 0L)
                ))
                .toList();
    }

    public Page<UsageLogAdminRow> searchUsageLogs(
            UsageLogAdminSearchCond cond,
            Pageable pageable
    ) {
        return usageLogRepository.searchAdminUsageLogs(cond, pageable);
    }

    private final MemberRepository memberRepository;

    public AdminMemberUsageSummaryResponse getMemberUsageSummary(Long memberId) {
        Member member = memberRepository.getByIdThrow(memberId);

        Long totalLogCount = usageLogRepository.countByMember_MemberId(memberId);
        Long totalTokenUsage = usageLogRepository.sumTokenUsageByMemberId(memberId);
        Long resumeUsageCount = usageLogRepository.countByMember_MemberIdAndServiceType(memberId, ServiceType.RESUME);
        Long interviewUsageCount = usageLogRepository.countByMember_MemberIdAndServiceType(memberId, ServiceType.INTERVIEW);

        if (totalTokenUsage == null) {
            totalTokenUsage = 0L;
        }

        return new AdminMemberUsageSummaryResponse(
                member.getMemberId(),
                member.getEmail(),
                member.getCreditBalance(),
                totalLogCount,
                totalTokenUsage,
                resumeUsageCount,
                interviewUsageCount
        );
    }

    public AdminServiceUsageSummaryResponse getServiceUsageSummary() {

        long resumeUsage =
                usageLogRepository.countByServiceType(ServiceType.RESUME);

        long interviewUsage =
                usageLogRepository.countByServiceType(ServiceType.INTERVIEW);

        long adminOperations =
                usageLogRepository.countByServiceType(ServiceType.ADMIN);

        long totalUsage = resumeUsage + interviewUsage + adminOperations;

        return new AdminServiceUsageSummaryResponse(
                resumeUsage,
                interviewUsage,
                adminOperations,
                totalUsage
        );
    }
}