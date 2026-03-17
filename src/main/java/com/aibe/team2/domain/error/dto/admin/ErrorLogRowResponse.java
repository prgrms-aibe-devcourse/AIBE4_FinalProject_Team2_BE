package com.aibe.team2.domain.error.dto.admin;

import com.aibe.team2.domain.error.entity.ErrorLog;
import com.aibe.team2.domain.error.enums.ErrorDomain;
import com.aibe.team2.domain.error.enums.ErrorSeverity;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ErrorLogRowResponse {

    private final Long logId;
    private final Long issueId;
    private final Long memberId;
    private final String errorCode;
    private final String exceptionType;
    private final String message;
    private final String normalizedMessage;
    private final String fingerprint;
    private final ErrorSeverity severity;
    private final ErrorDomain errorDomain;
    private final String requestTraceId;
    private final String targetType;
    private final Long targetId;
    private final LocalDateTime occurredAt;
    private final LocalDateTime createdAt;

    public ErrorLogRowResponse(ErrorLog log) {
        this.logId = log.getId();
        this.issueId = log.getIssue() != null ? log.getIssue().getId() : null;
        this.memberId = log.getMember() != null ? log.getMember().getMemberId() : null;
        this.errorCode = log.getErrorCode();
        this.exceptionType = log.getExceptionType();
        this.message = log.getMessage();
        this.normalizedMessage = log.getNormalizedMessage();
        this.fingerprint = log.getFingerprint();
        this.severity = log.getSeverity();
        this.errorDomain = log.getErrorDomain();
        this.requestTraceId = log.getRequestTraceId();
        this.targetType = log.getTargetType();
        this.targetId = log.getTargetId();
        this.occurredAt = log.getOccurredAt();
        this.createdAt = log.getCreatedAt();
    }
}