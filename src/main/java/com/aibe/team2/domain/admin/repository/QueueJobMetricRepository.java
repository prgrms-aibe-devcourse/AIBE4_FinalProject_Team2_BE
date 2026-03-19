package com.aibe.team2.domain.admin.repository;

import com.aibe.team2.domain.admin.entity.QueueJobMetric;
import com.aibe.team2.domain.admin.enums.QueueJobStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface QueueJobMetricRepository extends JpaRepository<QueueJobMetric, Long> {

    long countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(LocalDateTime start, LocalDateTime end);

    long countByStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
            QueueJobStatus status,
            LocalDateTime start,
            LocalDateTime end
    );

    long countByTargetTypeAndTargetIdAndStatus(String targetType, Long targetId, QueueJobStatus status);

    long countByStatus(QueueJobStatus status);

    long countByStatusAndCreatedAtBetween(
            QueueJobStatus status,
            LocalDateTime start,
            LocalDateTime end
    );

    Optional<QueueJobMetric> findTopByTargetTypeAndTargetIdOrderByCreatedAtDesc(String targetType, Long targetId);
}