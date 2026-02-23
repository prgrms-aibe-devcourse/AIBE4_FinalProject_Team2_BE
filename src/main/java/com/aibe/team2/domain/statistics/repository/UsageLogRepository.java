package com.aibe.team2.domain.statistics.repository;

import com.aibe.team2.domain.statistics.entity.UsageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface UsageLogRepository extends JpaRepository<UsageLog,Long> {

    // 특정 회원이 이번 달에 특정 서비스를 몇 번 썼는지 카운트
    long countByMemberIdAndServiceTypeAndCreatedAtBetween(
            Long memberId,
            String serviceType,
            LocalDateTime start,
            LocalDateTime end
    );
}
