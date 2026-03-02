package com.aibe.team2.domain.statistics.service;

import com.aibe.team2.domain.mypage.entity.Member;
import com.aibe.team2.domain.statistics.entity.UsageLog;
import com.aibe.team2.domain.statistics.enums.ServiceType;
import com.aibe.team2.domain.statistics.repository.usage.UsageLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UsageLogWriter {

    private static final String MDC_REQUEST_ID_KEY = "requestId";
    private static final String DEFAULT_TRACE_ID = "UNKNOWN";

    private final UsageLogRepository usageLogRepository;

    @Transactional
    public void record(
            Member memberRef,
            ServiceType serviceType,
            int amount,
            int tokenUsage,
            int balanceAfter,
            String targetType,
            Long targetId,
            String description
    ) {
        String traceId = resolveTraceId();

        UsageLog usageLog = UsageLog.of(
                memberRef,
                traceId,
                serviceType,
                amount,
                tokenUsage,
                balanceAfter,
                targetType,
                targetId,
                description
        );

        usageLogRepository.save(usageLog);

        log.debug("UsageLog saved. memberId={}, serviceType={}, amount={}, tokenUsage={}, balanceAfter={}, traceId={}",
                memberRef.getMemberId(), serviceType, amount, tokenUsage, balanceAfter, traceId);
    }

    private String resolveTraceId() {
        String traceId = MDC.get(MDC_REQUEST_ID_KEY);
        return (traceId == null || traceId.isBlank()) ? DEFAULT_TRACE_ID : traceId;
    }
}