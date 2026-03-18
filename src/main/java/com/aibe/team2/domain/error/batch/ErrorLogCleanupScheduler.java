package com.aibe.team2.domain.error.batch;

import com.aibe.team2.domain.error.repository.ErrorLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ErrorLogCleanupScheduler {

    private static final int RETENTION_DAYS = 30;

    private final ErrorLogRepository errorLogRepository;

    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void cleanupOldErrorLogs() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(RETENTION_DAYS);
        int deletedCount = errorLogRepository.deleteOldLogs(threshold);

        log.info("ErrorLog cleanup completed. retentionDays={}, deletedCount={}, threshold={}",
                RETENTION_DAYS, deletedCount, threshold);
    }
}