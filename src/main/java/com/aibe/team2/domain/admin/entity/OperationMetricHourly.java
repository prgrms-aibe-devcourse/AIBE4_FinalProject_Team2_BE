package com.aibe.team2.domain.admin.entity;

import com.aibe.team2.domain.statistics.enums.ServiceType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "operation_metric_hourly",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_operation_metric_hourly",
                        columnNames = {"metric_date", "metric_hour", "service_type"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class OperationMetricHourly {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "metric_date", nullable = false)
    private LocalDate metricDate;

    @Column(name = "metric_hour", nullable = false)
    private Integer metricHour;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 50)
    private ServiceType serviceType;

    @Column(name = "total_log_count", nullable = false)
    private Long totalLogCount;

    @Column(name = "total_token_usage", nullable = false)
    private Long totalTokenUsage;

    @Column(name = "queue_enqueued_count", nullable = false)
    private Long queueEnqueuedCount;

    @Column(name = "queue_success_count", nullable = false)
    private Long queueSuccessCount;

    @Column(name = "queue_failed_count", nullable = false)
    private Long queueFailedCount;

    @Column(name = "error_count", nullable = false)
    private Long errorCount;

    @Column(name = "failure_rate", nullable = false)
    private Double failureRate;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    private OperationMetricHourly(
            LocalDate metricDate,
            Integer metricHour,
            ServiceType serviceType,
            Long totalLogCount,
            Long totalTokenUsage,
            Long queueEnqueuedCount,
            Long queueSuccessCount,
            Long queueFailedCount,
            Long errorCount,
            Double failureRate
    ) {
        this.metricDate = metricDate;
        this.metricHour = metricHour;
        this.serviceType = serviceType;
        this.totalLogCount = totalLogCount;
        this.totalTokenUsage = totalTokenUsage;
        this.queueEnqueuedCount = queueEnqueuedCount;
        this.queueSuccessCount = queueSuccessCount;
        this.queueFailedCount = queueFailedCount;
        this.errorCount = errorCount;
        this.failureRate = failureRate;
    }

    public static OperationMetricHourly create(
            LocalDate metricDate,
            Integer metricHour,
            ServiceType serviceType,
            Long totalLogCount,
            Long totalTokenUsage,
            Long queueEnqueuedCount,
            Long queueSuccessCount,
            Long queueFailedCount,
            Long errorCount,
            Double failureRate
    ) {
        return OperationMetricHourly.builder()
                .metricDate(metricDate)
                .metricHour(metricHour)
                .serviceType(serviceType)
                .totalLogCount(totalLogCount)
                .totalTokenUsage(totalTokenUsage)
                .queueEnqueuedCount(queueEnqueuedCount)
                .queueSuccessCount(queueSuccessCount)
                .queueFailedCount(queueFailedCount)
                .errorCount(errorCount)
                .failureRate(failureRate)
                .build();
    }
}