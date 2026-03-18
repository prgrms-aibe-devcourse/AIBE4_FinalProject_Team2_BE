package com.aibe.team2.domain.admin.service;

import com.aibe.team2.domain.admin.entity.QueueJobMetric;
import com.aibe.team2.domain.admin.enums.QueueJobStatus;
import com.aibe.team2.domain.admin.enums.QueueJobType;
import com.aibe.team2.domain.admin.repository.QueueJobMetricRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueJobMetricService {

    private final QueueJobMetricRepository queueJobMetricRepository;

    @Transactional
    public Long recordEnqueued(
            QueueJobType jobType,
            String targetType,
            Long targetId,
            String messageId,
            Integer retryCount
    ) {
        QueueJobMetric metric = QueueJobMetric.create(
                jobType,
                QueueJobStatus.ENQUEUED,
                targetType,
                targetId,
                messageId,
                retryCount
        );
        return queueJobMetricRepository.save(metric).getId();
    }

    @Transactional
    public void markProcessing(Long queueJobMetricId) {
        queueJobMetricRepository.findById(queueJobMetricId)
                .ifPresent(QueueJobMetric::markProcessing);
    }

    @Transactional
    public void markSuccess(Long queueJobMetricId) {
        queueJobMetricRepository.findById(queueJobMetricId)
                .ifPresent(QueueJobMetric::markSuccess);
    }

    @Transactional
    public void markFailed(Long queueJobMetricId, String errorMessage) {
        queueJobMetricRepository.findById(queueJobMetricId)
                .ifPresent(metric -> metric.markFailed(trim(errorMessage)));
    }

    @Transactional
    public void markRetried(Long queueJobMetricId) {
        queueJobMetricRepository.findById(queueJobMetricId)
                .ifPresent(QueueJobMetric::markRetried);
    }

    private String trim(String value) {
        if (value == null) return null;
        return value.length() > 1000 ? value.substring(0, 1000) : value;
    }
}