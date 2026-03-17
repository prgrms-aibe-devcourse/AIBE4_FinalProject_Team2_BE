package com.aibe.team2.domain.error.service;

import com.aibe.team2.domain.error.entity.ErrorIssue;
import com.aibe.team2.domain.error.enums.ErrorDomain;
import com.aibe.team2.domain.error.enums.ErrorSeverity;
import com.aibe.team2.domain.error.repository.ErrorIssueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ErrorIssueService {

    private final ErrorIssueRepository errorIssueRepository;

    public ErrorIssue getOrCreateIssue(
            String fingerprint,
            String title,
            String errorCode,
            ErrorSeverity severity,
            ErrorDomain errorDomain,
            LocalDateTime occurredAt
    ) {
        return errorIssueRepository.findByFingerprint(fingerprint)
                .orElseGet(() -> errorIssueRepository.save(
                        ErrorIssue.create(
                                fingerprint,
                                title,
                                errorCode,
                                severity,
                                errorDomain,
                                occurredAt
                        )
                ));
    }

    public void increaseOccurrence(ErrorIssue issue, LocalDateTime occurredAt, Long errorLogId) {
        issue.increaseOccurrence(occurredAt, errorLogId);
    }
}