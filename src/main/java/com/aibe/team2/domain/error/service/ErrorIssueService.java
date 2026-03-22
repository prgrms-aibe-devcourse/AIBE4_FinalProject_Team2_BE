package com.aibe.team2.domain.error.service;

import com.aibe.team2.domain.error.config.ErrorAlertProperties;
import com.aibe.team2.domain.error.entity.ErrorIssue;
import com.aibe.team2.domain.error.enums.ErrorDomain;
import com.aibe.team2.domain.error.enums.ErrorSeverity;
import com.aibe.team2.domain.error.enums.IssueStatus;
import com.aibe.team2.domain.error.repository.ErrorIssueRepository;
import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ErrorIssueService {

    private final ErrorIssueRepository errorIssueRepository;
    private final ErrorAlertProperties errorAlertProperties;

    public ErrorIssue getOrCreateIssue(
            String fingerprint,
            String title,
            String errorCode,
            ErrorSeverity severity,
            ErrorDomain errorDomain,
            LocalDateTime occurredAt
    ) {
        return errorIssueRepository.findByFingerprint(fingerprint)
                .orElseGet(() -> createIssueSafely(
                        fingerprint,
                        title,
                        errorCode,
                        severity,
                        errorDomain,
                        occurredAt
                ));
    }

    private ErrorIssue createIssueSafely(
            String fingerprint,
            String title,
            String errorCode,
            ErrorSeverity severity,
            ErrorDomain errorDomain,
            LocalDateTime occurredAt
    ) {
        try {
            return errorIssueRepository.save(
                    ErrorIssue.create(
                            fingerprint,
                            title,
                            errorCode,
                            severity,
                            errorDomain,
                            occurredAt
                    )
            );
        } catch (DataIntegrityViolationException e) {
            return errorIssueRepository.findByFingerprint(fingerprint)
                    .orElseThrow(() -> e);
        }
    }

    public void increaseOccurrence(ErrorIssue issue, LocalDateTime occurredAt, Long errorLogId) {
        issue.increaseOccurrence(occurredAt, errorLogId);
        checkAlertThreshold(issue);
    }

    public void updateIssueStatus(Long issueId, IssueStatus status) {
        ErrorIssue issue = errorIssueRepository.findById(issueId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ERROR_ISSUE_NOT_FOUND));

        issue.updateStatus(status);
    }

    private void checkAlertThreshold(ErrorIssue issue) {
        long threshold = errorAlertProperties.getOccurrenceThreshold();

        if (issue.getOccurrenceCount() >= threshold) {
            log.warn(
                    "Error issue threshold exceeded. issueId={}, errorCode={}, title={}, severity={}, domain={}, occurrenceCount={}, threshold={}",
                    issue.getId(),
                    issue.getErrorCode(),
                    issue.getTitle(),
                    issue.getSeverity(),
                    issue.getErrorDomain(),
                    issue.getOccurrenceCount(),
                    threshold
            );
        }
    }
}