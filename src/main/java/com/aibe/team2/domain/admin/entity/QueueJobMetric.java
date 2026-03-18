package com.aibe.team2.domain.admin.entity;

import com.aibe.team2.domain.admin.enums.QueueJobStatus;
import com.aibe.team2.domain.admin.enums.QueueJobType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "queue_job_metric",
        indexes = {
                @Index(name = "idx_queue_job_target", columnList = "target_type,target_id"),
                @Index(name = "idx_queue_job_status", columnList = "status"),
                @Index(name = "idx_queue_job_created_at", columnList = "created_at")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class QueueJobMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false, length = 50)
    private QueueJobType jobType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private QueueJobStatus status;

    @Column(name = "target_type", nullable = false, length = 50)
    private String targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "message_id", length = 100)
    private String messageId;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    private QueueJobMetric(
            QueueJobType jobType,
            QueueJobStatus status,
            String targetType,
            Long targetId,
            String messageId,
            Integer retryCount,
            String errorMessage
    ) {
        this.jobType = jobType;
        this.status = status;
        this.targetType = targetType;
        this.targetId = targetId;
        this.messageId = messageId;
        this.retryCount = retryCount;
        this.errorMessage = errorMessage;
    }

    public static QueueJobMetric create(
            QueueJobType jobType,
            QueueJobStatus status,
            String targetType,
            Long targetId,
            String messageId,
            Integer retryCount
    ) {
        return QueueJobMetric.builder()
                .jobType(jobType)
                .status(status)
                .targetType(targetType)
                .targetId(targetId)
                .messageId(messageId)
                .retryCount(retryCount == null ? 0 : retryCount)
                .build();
    }

    public void markProcessing() {
        this.status = QueueJobStatus.PROCESSING;
    }

    public void markSuccess() {
        this.status = QueueJobStatus.SUCCESS;
        this.errorMessage = null;
    }

    public void markFailed(String errorMessage) {
        this.status = QueueJobStatus.FAILED;
        this.errorMessage = errorMessage;
    }

    public void markRetried() {
        this.status = QueueJobStatus.RETRIED;
        this.retryCount = this.retryCount + 1;
    }

    public void markCancelled(String reason) {
        this.status = QueueJobStatus.CANCELLED;
        this.errorMessage = reason;
    }
}