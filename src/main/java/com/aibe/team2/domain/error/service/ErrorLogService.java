package com.aibe.team2.domain.error.service;

import com.aibe.team2.domain.error.entity.ErrorIssue;
import com.aibe.team2.domain.error.entity.ErrorLog;
import com.aibe.team2.domain.error.enums.ErrorDomain;
import com.aibe.team2.domain.error.enums.ErrorSeverity;
import com.aibe.team2.domain.error.repository.ErrorLogRepository;
import com.aibe.team2.domain.error.util.ErrorFingerprintGenerator;
import com.aibe.team2.domain.error.util.ErrorSeverityResolver;
import com.aibe.team2.domain.mypage.entity.Member;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ErrorLogService {

    private static final String MDC_REQUEST_ID_KEY = "requestId";
    private static final String DEFAULT_TRACE_ID = "UNKNOWN";

    private final ErrorLogRepository errorLogRepository;
    private final ErrorIssueService errorIssueService;
    private final ErrorFingerprintGenerator fingerprintGenerator;
    private final ErrorSeverityResolver severityResolver;
    private final MeterRegistry meterRegistry;

    @Async("errorLogExecutor")
    @Transactional
    public void record(
            String errorCode,
            ErrorDomain errorDomain,
            Throwable throwable,
            Member member,
            String targetType,
            Long targetId
    ) {
        LocalDateTime occurredAt = LocalDateTime.now();

        String exceptionType = throwable != null
                ? throwable.getClass().getName()
                : "UnknownException";

        String exceptionSimpleName = throwable != null
                ? throwable.getClass().getSimpleName()
                : "UnknownException";

        String message = throwable != null && throwable.getMessage() != null
                ? throwable.getMessage()
                : "No exception message";

        String shortMessage = message.length() > 80
                ? message.substring(0, 80)
                : message;

        String issueTitle = exceptionSimpleName + " | " + shortMessage;

        String normalizedMessage = fingerprintGenerator.normalizeMessage(message);
        String fingerprint = fingerprintGenerator.generate(errorCode, exceptionType, message);
        ErrorSeverity severity = severityResolver.resolve(errorCode, throwable);
        String stackTrace = extractStackTrace(throwable);
        String traceId = resolveTraceId();

        ErrorIssue issue = errorIssueService.getOrCreateIssue(
                fingerprint,
                issueTitle,
                errorCode,
                severity,
                errorDomain,
                occurredAt
        );

        ErrorLog errorLog = errorLogRepository.save(
                ErrorLog.create(
                        issue,
                        member,
                        errorCode,
                        exceptionType,
                        message,
                        normalizedMessage,
                        fingerprint,
                        severity,
                        errorDomain,
                        traceId,
                        targetType,
                        targetId,
                        stackTrace,
                        occurredAt
                )
        );

        errorIssueService.increaseOccurrence(issue, occurredAt, errorLog.getId());

        meterRegistry.counter("error.log.count",
                "domain", errorDomain.name(),
                "severity", severity.name()
        ).increment();
    }

    private String resolveTraceId() {
        String traceId = MDC.get(MDC_REQUEST_ID_KEY);
        return (traceId == null || traceId.isBlank()) ? DEFAULT_TRACE_ID : traceId;
    }

    private String extractStackTrace(Throwable throwable) {
        if (throwable == null) {
            return null;
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
}