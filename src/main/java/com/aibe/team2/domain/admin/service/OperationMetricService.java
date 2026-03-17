package com.aibe.team2.domain.admin.service;

import com.aibe.team2.domain.admin.entity.OperationMetricDaily;
import com.aibe.team2.domain.admin.enums.QueueJobStatus;
import com.aibe.team2.domain.admin.repository.OperationMetricDailyRepository;
import com.aibe.team2.domain.admin.repository.QueueJobMetricRepository;
import com.aibe.team2.domain.error.repository.ErrorLogRepository;
import com.aibe.team2.domain.statistics.enums.ServiceType;
import com.aibe.team2.domain.statistics.repository.usage.UsageLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OperationMetricService {

    private final OperationMetricDailyRepository operationMetricDailyRepository;
    private final UsageLogRepository usageLogRepository;
    private final QueueJobMetricRepository queueJobMetricRepository;
    private final ErrorLogRepository errorLogRepository;

    @Transactional
    public void aggregateDaily(LocalDate targetDate) {
        LocalDateTime start = targetDate.atStartOfDay();
        LocalDateTime end = targetDate.plusDays(1).atStartOfDay();

        for (ServiceType serviceType : List.of(ServiceType.RESUME, ServiceType.INTERVIEW, ServiceType.ADMIN)) {
            long totalLogCount = usageLogRepository.countByServiceTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                    serviceType, start, end
            );

            Long totalTokenUsage = usageLogRepository.sumTokenUsageByServiceTypeAndCreatedAtRange(
                    serviceType, start, end
            );

            long errorCount = errorLogRepository.countByOccurredAtBetween(start, end);

            long queueEnqueuedCount = serviceType == ServiceType.RESUME
                    ? queueJobMetricRepository.countByStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                    QueueJobStatus.ENQUEUED, start, end)
                    : 0;

            long queueSuccessCount = serviceType == ServiceType.RESUME
                    ? queueJobMetricRepository.countByStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                    QueueJobStatus.SUCCESS, start, end)
                    : 0;

            long queueFailedCount = serviceType == ServiceType.RESUME
                    ? queueJobMetricRepository.countByStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                    QueueJobStatus.FAILED, start, end)
                    : 0;

            operationMetricDailyRepository.findByMetricDateAndServiceType(targetDate, serviceType)
                    .orElseGet(() -> operationMetricDailyRepository.save(
                            OperationMetricDaily.create(
                                    targetDate,
                                    serviceType,
                                    totalLogCount,
                                    totalTokenUsage == null ? 0L : totalTokenUsage,
                                    queueEnqueuedCount,
                                    queueSuccessCount,
                                    queueFailedCount,
                                    errorCount
                            )
                    ));
        }
    }
}