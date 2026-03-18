package com.aibe.team2.domain.admin.service;

import com.aibe.team2.domain.admin.entity.OperationMetricHourly;
import com.aibe.team2.domain.admin.enums.QueueJobStatus;
import com.aibe.team2.domain.admin.repository.OperationMetricHourlyRepository;
import com.aibe.team2.domain.admin.repository.QueueJobMetricRepository;
import com.aibe.team2.domain.error.enums.ErrorDomain;
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
public class OperationMetricHourlyService {

    private final OperationMetricHourlyRepository operationMetricHourlyRepository;
    private final UsageLogRepository usageLogRepository;
    private final QueueJobMetricRepository queueJobMetricRepository;
    private final ErrorLogRepository errorLogRepository;

    @Transactional
    public void aggregateHourly(LocalDate targetDate, int targetHour) {
        LocalDateTime start = targetDate.atTime(targetHour, 0);
        LocalDateTime end = start.plusHours(1);

        for (ServiceType serviceType : List.of(ServiceType.RESUME, ServiceType.INTERVIEW, ServiceType.ADMIN)) {
            long totalLogCount =
                    usageLogRepository.countByServiceTypeAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                            serviceType, start, end
                    );

            Long totalTokenUsage =
                    usageLogRepository.sumTokenUsageByServiceTypeAndCreatedAtRange(
                            serviceType, start, end
                    );

            ErrorDomain errorDomain = mapServiceTypeToErrorDomain(serviceType);
            long errorCount =
                    errorLogRepository.countByErrorDomainAndOccurredAtBetween(
                            errorDomain, start, end
                    );

            long queueEnqueuedCount = serviceType == ServiceType.RESUME
                    ? queueJobMetricRepository.countByStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                    QueueJobStatus.ENQUEUED, start, end
            )
                    : 0L;

            long queueSuccessCount = serviceType == ServiceType.RESUME
                    ? queueJobMetricRepository.countByStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                    QueueJobStatus.SUCCESS, start, end
            )
                    : 0L;

            long queueFailedCount = serviceType == ServiceType.RESUME
                    ? queueJobMetricRepository.countByStatusAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                    QueueJobStatus.FAILED, start, end
            )
                    : 0L;

            double failureRate = queueEnqueuedCount == 0
                    ? 0.0
                    : (double) queueFailedCount / queueEnqueuedCount;

            operationMetricHourlyRepository.findByMetricDateAndMetricHourAndServiceType(
                    targetDate, targetHour, serviceType
            ).orElseGet(() -> operationMetricHourlyRepository.save(
                    OperationMetricHourly.create(
                            targetDate,
                            targetHour,
                            serviceType,
                            totalLogCount,
                            totalTokenUsage == null ? 0L : totalTokenUsage,
                            queueEnqueuedCount,
                            queueSuccessCount,
                            queueFailedCount,
                            errorCount,
                            failureRate
                    )
            ));
        }
    }

    private ErrorDomain mapServiceTypeToErrorDomain(ServiceType serviceType) {
        return switch (serviceType) {
            case RESUME -> ErrorDomain.RESUME;
            case INTERVIEW -> ErrorDomain.INTERVIEW;
            case ADMIN -> ErrorDomain.ADMIN;
        };
    }
}