package com.aibe.team2.domain.error.dto.admin;

import com.aibe.team2.domain.error.entity.ErrorIssue;
import com.aibe.team2.domain.error.enums.ErrorDomain;
import com.aibe.team2.domain.error.enums.ErrorSeverity;
import com.aibe.team2.domain.error.enums.IssueStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ErrorIssueDetailResponse {

    private final Long issueId;
    private final String fingerprint;
    private final String title;
    private final String errorCode;
    private final ErrorSeverity severity;
    private final IssueStatus status;
    private final ErrorDomain errorDomain;
    private final Long occurrenceCount;
    private final LocalDateTime firstOccurredAt;
    private final LocalDateTime lastOccurredAt;
    private final Long lastErrorLogId;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private final String targetType;
    private final Long targetId;
    private final Long memberId;
    private final String memberEmail;
    private final String memberNickname;

    public ErrorIssueDetailResponse(
            ErrorIssue issue,
            String targetType,
            Long targetId,
            Long memberId,
            String memberEmail,
            String memberNickname
    ) {
        this.issueId = issue.getId();
        this.fingerprint = issue.getFingerprint();
        this.title = issue.getTitle();
        this.errorCode = issue.getErrorCode();
        this.severity = issue.getSeverity();
        this.status = issue.getStatus();
        this.errorDomain = issue.getErrorDomain();
        this.occurrenceCount = issue.getOccurrenceCount();
        this.firstOccurredAt = issue.getFirstOccurredAt();
        this.lastOccurredAt = issue.getLastOccurredAt();
        this.lastErrorLogId = issue.getLastErrorLogId();
        this.createdAt = issue.getCreatedAt();
        this.updatedAt = issue.getUpdatedAt();

        this.targetType = targetType;
        this.targetId = targetId;
        this.memberId = memberId;
        this.memberEmail = memberEmail;
        this.memberNickname = memberNickname;
    }
}