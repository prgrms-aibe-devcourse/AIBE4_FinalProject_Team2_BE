package com.aibe.team2.domain.error.entity;

import com.aibe.team2.domain.error.enums.ErrorDomain;
import com.aibe.team2.domain.error.enums.ErrorSeverity;
import com.aibe.team2.domain.error.enums.IssueStatus;
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
@Table(name = "error_issue",
        indexes = {
                @Index(name = "idx_error_issue_fingerprint", columnList = "fingerprint", unique = true),
                @Index(name = "idx_error_issue_status", columnList = "status"),
                @Index(name = "idx_error_issue_severity", columnList = "severity"),
                @Index(name = "idx_error_issue_last_occurred_at", columnList = "last_occurred_at")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ErrorIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fingerprint", nullable = false, length = 255, unique = true)
    private String fingerprint;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "error_code", length = 100)
    private String errorCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 30)
    private ErrorSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private IssueStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "error_domain", nullable = false, length = 50)
    private ErrorDomain errorDomain;

    @Column(name = "occurrence_count", nullable = false)
    private Long occurrenceCount;

    @Column(name = "first_occurred_at", nullable = false)
    private LocalDateTime firstOccurredAt;

    @Column(name = "last_occurred_at", nullable = false)
    private LocalDateTime lastOccurredAt;

    @Column(name = "last_error_log_id")
    private Long lastErrorLogId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    private ErrorIssue(
            String fingerprint,
            String title,
            String errorCode,
            ErrorSeverity severity,
            IssueStatus status,
            ErrorDomain errorDomain,
            Long occurrenceCount,
            LocalDateTime firstOccurredAt,
            LocalDateTime lastOccurredAt,
            Long lastErrorLogId
    ) {
        this.fingerprint = fingerprint;
        this.title = title;
        this.errorCode = errorCode;
        this.severity = severity;
        this.status = status;
        this.errorDomain = errorDomain;
        this.occurrenceCount = occurrenceCount;
        this.firstOccurredAt = firstOccurredAt;
        this.lastOccurredAt = lastOccurredAt;
        this.lastErrorLogId = lastErrorLogId;
    }

    public static ErrorIssue create(
            String fingerprint,
            String title,
            String errorCode,
            ErrorSeverity severity,
            ErrorDomain errorDomain,
            LocalDateTime occurredAt
    ) {
        return ErrorIssue.builder()
                .fingerprint(fingerprint)
                .title(title)
                .errorCode(errorCode)
                .severity(severity)
                .status(IssueStatus.OPEN)
                .errorDomain(errorDomain)
                .occurrenceCount(0L)
                .firstOccurredAt(occurredAt)
                .lastOccurredAt(occurredAt)
                .build();
    }

    public void increaseOccurrence(LocalDateTime occurredAt, Long errorLogId) {
        this.occurrenceCount++;
        this.lastOccurredAt = occurredAt;
        this.lastErrorLogId = errorLogId;
    }

    public void updateStatus(IssueStatus status) {
        this.status = status;
    }
}