package com.aibe.team2.domain.admin.batch;

import com.aibe.team2.domain.admin.service.OperationMetricHourlyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class OperationMetricHourlyScheduler {

    private final OperationMetricHourlyService operationMetricHourlyService;

    // 매시 5분에 직전 1시간 집계
    @Scheduled(cron = "0 5 * * * *")
    public void aggregatePreviousHour() {
        LocalDateTime now = LocalDateTime.now().minusHours(1);

        LocalDate targetDate = now.toLocalDate();
        int targetHour = now.getHour();

        log.info("운영 시간 단위 집계 시작 - date={}, hour={}", targetDate, targetHour);
        operationMetricHourlyService.aggregateHourly(targetDate, targetHour);
        log.info("운영 시간 단위 집계 완료 - date={}, hour={}", targetDate, targetHour);
    }
}