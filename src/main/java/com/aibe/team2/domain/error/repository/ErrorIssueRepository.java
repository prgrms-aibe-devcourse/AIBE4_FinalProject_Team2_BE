package com.aibe.team2.domain.error.repository;

import com.aibe.team2.domain.error.entity.ErrorIssue;
import com.aibe.team2.domain.error.enums.ErrorDomain;
import com.aibe.team2.domain.error.enums.ErrorSeverity;
import com.aibe.team2.domain.error.enums.IssueStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface ErrorIssueRepository extends JpaRepository<ErrorIssue, Long>, ErrorIssueRepositoryCustom {

    Optional<ErrorIssue> findByFingerprint(String fingerprint);

    Page<ErrorIssue> findByStatus(IssueStatus status, Pageable pageable);

    Page<ErrorIssue> findBySeverity(ErrorSeverity severity, Pageable pageable);

    Page<ErrorIssue> findByErrorDomain(ErrorDomain errorDomain, Pageable pageable);

    Page<ErrorIssue> findByStatusAndSeverity(IssueStatus status, ErrorSeverity severity, Pageable pageable);

    Page<ErrorIssue> findByStatusAndErrorDomain(IssueStatus status, ErrorDomain errorDomain, Pageable pageable);

    Page<ErrorIssue> findBySeverityAndErrorDomain(ErrorSeverity severity, ErrorDomain errorDomain, Pageable pageable);

    Page<ErrorIssue> findByStatusAndSeverityAndErrorDomain(
            IssueStatus status,
            ErrorSeverity severity,
            ErrorDomain errorDomain,
            Pageable pageable
    );

    long countByStatus(IssueStatus status);

    long countByStatusIn(Collection<IssueStatus> statuses);
}