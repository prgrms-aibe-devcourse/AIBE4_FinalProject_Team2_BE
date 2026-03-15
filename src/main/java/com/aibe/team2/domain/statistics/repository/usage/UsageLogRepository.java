package com.aibe.team2.domain.statistics.repository.usage;

import com.aibe.team2.domain.statistics.entity.UsageLog;
import com.aibe.team2.domain.statistics.enums.ServiceType;
import com.aibe.team2.domain.statistics.dto.admin.DailyUsageAdminRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UsageLogRepository extends JpaRepository<UsageLog, Long>, UsageLogRepositoryCustom {

    /*
     * 특정 회원이 이번 달에 특정 서비스를 몇 번 썼는지 카운트
     * * [수정 포인트]
     * 1. ServiceType 타입을 String -> ServiceType(Enum)으로 변경
     * 2. Member 엔티티의 PK 필드명이 'memberId'이므로, 탐색 경로를 명확히 하기 위해
     * 'Member_MemberId' (Member 객체의 MemberId 필드) 형식 사용
     */

    @Query("SELECT COUNT(u) FROM UsageLog u " +
            "WHERE u.member.memberId = :memberId " +
            "AND u.serviceType = :serviceType " +
            "AND u.createdAt BETWEEN :start AND :end")

    long countMonthlyUsage(
            @Param("memberId") Long memberId,
            @Param("serviceType") ServiceType serviceType,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    long countByServiceTypeAndCreatedAtBetween(ServiceType serviceType, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COALESCE(SUM(u.tokenUsage), 0) FROM UsageLog u WHERE u.createdAt BETWEEN :start AND :end")
    Long sumTokenUsageByCreatedAtBetween(@Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);

    @Query("""
    select new com.aibe.team2.domain.statistics.dto.admin.DailyUsageAdminRow(
        u.serviceType,
        coalesce(sum(u.amount), 0L),
        coalesce(sum(u.tokenUsage), 0L),
        count(u)
    )
    from UsageLog u
    where u.createdAt >= :start and u.createdAt < :end
    group by u.serviceType
""")
    List<DailyUsageAdminRow> aggregateDailyAdmin(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    long countByMember_MemberId(Long memberId);

    long countByMember_MemberIdAndServiceType(Long memberId, ServiceType serviceType);

    @Query("""
select coalesce(sum(u.tokenUsage), 0)
from UsageLog u
where u.member.memberId = :memberId
""")
    Long sumTokenUsageByMemberId(Long memberId);

    long countByServiceType(ServiceType serviceType);

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime start, LocalDateTime end);

    long countByServiceTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            ServiceType serviceType,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
select coalesce(sum(u.tokenUsage), 0)
from UsageLog u
where u.serviceType = :serviceType
""")
    Long sumTokenUsageByServiceType(@Param("serviceType") ServiceType serviceType);

    @Query("""
select coalesce(sum(u.tokenUsage), 0)
from UsageLog u
where u.serviceType in :serviceTypes
""")
    Long sumTokenUsageByServiceTypes(@Param("serviceTypes") List<ServiceType> serviceTypes);

    @Query("""
select coalesce(sum(u.tokenUsage), 0)
from UsageLog u
where u.member.memberId = :memberId
  and u.serviceType = :serviceType
""")
    Long sumTokenUsageByMemberIdAndServiceType(
            @Param("memberId") Long memberId,
            @Param("serviceType") ServiceType serviceType
    );

    @Query("""
select coalesce(sum(u.tokenUsage), 0)
from UsageLog u
where u.member.memberId = :memberId
  and u.serviceType in :serviceTypes
""")
    Long sumTokenUsageByMemberIdAndServiceTypes(
            @Param("memberId") Long memberId,
            @Param("serviceTypes") List<ServiceType> serviceTypes
    );

    @Query("""
select coalesce(sum(u.tokenUsage), 0)
from UsageLog u
where u.createdAt >= :start and u.createdAt < :end
""")
    Long sumTokenUsageByCreatedAtRange(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
