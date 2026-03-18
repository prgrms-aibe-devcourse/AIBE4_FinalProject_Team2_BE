package com.aibe.team2.domain.admin.service;

import com.aibe.team2.domain.admin.dto.ops.OpsAlertResponse;
import com.aibe.team2.domain.admin.dto.ops.OpsDashboardSummaryResponse;
import com.aibe.team2.domain.admin.dto.ops.OpsQueueHourlyResponse;
import com.aibe.team2.domain.admin.dto.ops.OpsQueueSummaryResponse;
import com.aibe.team2.domain.admin.enums.QueueJobStatus;
import com.aibe.team2.domain.admin.repository.QueueJobMetricRepository;
import com.aibe.team2.domain.error.enums.IssueStatus;
import com.aibe.team2.domain.error.repository.ErrorIssueRepository;
import com.aibe.team2.domain.error.repository.ErrorLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OpsMonitoringService {

    private final QueueJobMetricRepository queueJobMetricRepository;
    private final ErrorLogRepository errorLogRepository;
    private final ErrorIssueRepository errorIssueRepository;

    @Value("${app.error.alert.occurrence-threshold:10}")
    private long alertThreshold;

    @Transactional(readOnly = true)
    public OpsDashboardSummaryResponse getDashboardSummary() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        long todayErrorCount = errorLogRepository.countByOccurredAtBetween(start, end);
        long todayQueueEnqueuedCount = queueJobMetricRepository.countByStatusAndCreatedAtBetween(QueueJobStatus.ENQUEUED, start, end);
        long todayQueueSuccessCount = queueJobMetricRepository.countByStatusAndCreatedAtBetween(QueueJobStatus.SUCCESS, start, end);
        long todayQueueFailedCount = queueJobMetricRepository.countByStatusAndCreatedAtBetween(QueueJobStatus.FAILED, start, end);

        long totalDone = todayQueueSuccessCount + todayQueueFailedCount;
        double successRate = totalDone == 0 ? 0.0 : (todayQueueSuccessCount * 100.0 / totalDone);

        long unresolvedIssueCount = errorIssueRepository.countByStatus(IssueStatus.OPEN);

        return OpsDashboardSummaryResponse.builder()
                .todayErrorCount(todayErrorCount)
                .todayQueueEnqueuedCount(todayQueueEnqueuedCount)
                .todayQueueSuccessCount(todayQueueSuccessCount)
                .todayQueueFailedCount(todayQueueFailedCount)
                .todayQueueSuccessRate(successRate)
                .unresolvedIssueCount(unresolvedIssueCount)
                .build();
    }

    @Transactional(readOnly = true)
    public List<OpsAlertResponse> getAlerts() {
        List<OpsAlertResponse> alerts = new ArrayList<>();

        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        long failedCount = queueJobMetricRepository.countByStatusAndCreatedAtBetween(
                QueueJobStatus.FAILED, start, end
        );

        if (failedCount >= alertThreshold) {
            alerts.add(OpsAlertResponse.builder()
                    .alertType("QUEUE_FAILURE")
                    .severity("HIGH")
                    .message("오늘 실패한 큐 작업이 임계치 이상입니다. failedCount=" + failedCount)
                    .targetType("QUEUE_JOB")
                    .targetId(null)
                    .build());
        }

        long openIssueCount = errorIssueRepository.countByStatus(IssueStatus.OPEN);
        if (openIssueCount >= alertThreshold) {
            alerts.add(OpsAlertResponse.builder()
                    .alertType("OPEN_ERROR_ISSUE")
                    .severity("MEDIUM")
                    .message("미해결 에러 이슈가 임계치 이상입니다. openIssueCount=" + openIssueCount)
                    .targetType("ERROR_ISSUE")
                    .targetId(null)
                    .build());
        }

        return alerts;
    }

    @Transactional(readOnly = true)
    public OpsQueueSummaryResponse getQueueSummary() {
        long enqueued = queueJobMetricRepository.countByStatus(QueueJobStatus.ENQUEUED);
        long processing = queueJobMetricRepository.countByStatus(QueueJobStatus.PROCESSING);
        long success = queueJobMetricRepository.countByStatus(QueueJobStatus.SUCCESS);
        long failed = queueJobMetricRepository.countByStatus(QueueJobStatus.FAILED);
        long cancelled = queueJobMetricRepository.countByStatus(QueueJobStatus.CANCELLED);

        long totalDone = success + failed + cancelled;
        double successRate = totalDone == 0 ? 0.0 : (success * 100.0 / totalDone);

        return OpsQueueSummaryResponse.builder()
                .enqueuedCount(enqueued)
                .processingCount(processing)
                .successCount(success)
                .failedCount(failed)
                .cancelledCount(cancelled)
                .successRate(successRate)
                .build();
    }

    @Transactional(readOnly = true)
    public List<OpsQueueHourlyResponse> getQueueHourly(LocalDate date) {
        List<OpsQueueHourlyResponse> result = new ArrayList<>();

        for (int i = 0; i < 24; i++) {
            LocalDateTime start = date.atTime(i, 0);
            LocalDateTime end = start.plusHours(1);

            long enqueued = queueJobMetricRepository.countByStatusAndCreatedAtBetween(QueueJobStatus.ENQUEUED, start, end);
            long success = queueJobMetricRepository.countByStatusAndCreatedAtBetween(QueueJobStatus.SUCCESS, start, end);
            long failed = queueJobMetricRepository.countByStatusAndCreatedAtBetween(QueueJobStatus.FAILED, start, end);

            result.add(OpsQueueHourlyResponse.builder()
                    .hour(String.format("%02d:00", i))
                    .enqueuedCount(enqueued)
                    .successCount(success)
                    .failedCount(failed)
                    .build());
        }

        return result;
    }
}