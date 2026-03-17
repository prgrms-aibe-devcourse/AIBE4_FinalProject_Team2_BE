package com.aibe.team2.domain.error.entity;

import com.aibe.team2.domain.error.enums.ErrorDomain;
import com.aibe.team2.domain.error.enums.ErrorSeverity;
import com.aibe.team2.domain.mypage.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "error_log",
        indexes = {
                @Index(name = "idx_error_log_issue", columnList = "issue_id"),
                @Index(name = "idx_error_log_fingerprint", columnList = "fingerprint"),
                @Index(name = "idx_error_log_occurred_at", columnList = "occurred_at"),
                @Index(name = "idx_error_log_error_code", columnList = "error_code")
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ErrorLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id")
    private ErrorIssue issue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "error_code", length = 100)
    private String errorCode;

    @Column(name = "exception_type", nullable = false, length = 255)
    private String exceptionType;

    @Column(name = "message", nullable = false, length = 1000)
    private String message;

    @Column(name = "normalized_message", length = 1000)
    private String normalizedMessage;

    @Column(name = "fingerprint", nullable = false, length = 255)
    private String fingerprint;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 30)
    private ErrorSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "error_domain", nullable = false, length = 50)
    private ErrorDomain errorDomain;

    @Column(name = "request_trace_id", length = 100)
    private String requestTraceId;

    @Column(name = "target_type", length = 100)
    private String targetType;

    @Column(name = "target_id")
    private Long targetId;

    @Lob
    @Column(name = "stack_trace")
    private String stackTrace;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private ErrorLog(
            ErrorIssue issue,
            Member member,
            String errorCode,
            String exceptionType,
            String message,
            String normalizedMessage,
            String fingerprint,
            ErrorSeverity severity,
            ErrorDomain errorDomain,
            String requestTraceId,
            String targetType,
            Long targetId,
            String stackTrace,
            LocalDateTime occurredAt
    ) {
        this.issue = issue;
        this.member = member;
        this.errorCode = errorCode;
        this.exceptionType = exceptionType;
        this.message = message;
        this.normalizedMessage = normalizedMessage;
        this.fingerprint = fingerprint;
        this.severity = severity;
        this.errorDomain = errorDomain;
        this.requestTraceId = requestTraceId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.stackTrace = stackTrace;
        this.occurredAt = occurredAt;
    }

    public static ErrorLog create(
            ErrorIssue issue,
            Member member,
            String errorCode,
            String exceptionType,
            String message,
            String normalizedMessage,
            String fingerprint,
            ErrorSeverity severity,
            ErrorDomain errorDomain,
            String requestTraceId,
            String targetType,
            Long targetId,
            String stackTrace,
            LocalDateTime occurredAt
    ) {
        return ErrorLog.builder()
                .issue(issue)
                .member(member)
                .errorCode(errorCode)
                .exceptionType(exceptionType)
                .message(message)
                .normalizedMessage(normalizedMessage)
                .fingerprint(fingerprint)
                .severity(severity)
                .errorDomain(errorDomain)
                .requestTraceId(requestTraceId)
                .targetType(targetType)
                .targetId(targetId)
                .stackTrace(stackTrace)
                .occurredAt(occurredAt)
                .build();
    }
}