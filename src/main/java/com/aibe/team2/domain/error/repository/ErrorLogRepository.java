package com.aibe.team2.domain.error.repository;

import com.aibe.team2.domain.error.entity.ErrorLog;
import com.aibe.team2.domain.error.enums.ErrorDomain;
import com.aibe.team2.domain.error.enums.ErrorSeverity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ErrorLogRepository extends JpaRepository<ErrorLog, Long> {

    Page<ErrorLog> findByIssueId(Long issueId, Pageable pageable);

    Page<ErrorLog> findByErrorDomain(ErrorDomain errorDomain, Pageable pageable);

    Page<ErrorLog> findBySeverity(ErrorSeverity severity, Pageable pageable);

    Page<ErrorLog> findByErrorCode(String errorCode, Pageable pageable);

    Page<ErrorLog> findByErrorDomainAndSeverity(
            ErrorDomain errorDomain,
            ErrorSeverity severity,
            Pageable pageable
    );

    long countByOccurredAtBetween(LocalDateTime start, LocalDateTime end);

    long countByErrorDomainAndOccurredAtBetween(
            ErrorDomain errorDomain,
            LocalDateTime start,
            LocalDateTime end
    );

    Optional<ErrorLog> findTopByTargetTypeAndTargetIdOrderByOccurredAtDesc(String targetType, Long targetId);

    Optional<ErrorLog> findTopByIssueIdOrderByOccurredAtDesc(Long issueId);

    @Modifying
    @Query("delete from ErrorLog e where e.occurredAt < :threshold")
    int deleteOldLogs(@Param("threshold") LocalDateTime threshold);
}