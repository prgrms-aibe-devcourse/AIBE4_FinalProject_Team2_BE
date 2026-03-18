package com.aibe.team2.domain.resume.service;

import com.aibe.team2.domain.resume.dto.AnalysisEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalysisQueueProducer {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String QUEUE_KEY = "resume:analysis:queue";
    private final com.aibe.team2.domain.admin.service.QueueJobMetricService queueJobMetricService;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AnalysisMessage {
        private Long reportId;
        private String resumeContent;
        private Long queueJobMetricId;
        private Integer retryCount;
    }

    // DB 트랜잭션이 성공적으로 커밋된 직후에만 실행됨 (데이터 유실 완벽 방지)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleResumeAnalysisEvent(AnalysisEvent event) {
        String messageId = "resume-" + event.reportId() + "-" + System.currentTimeMillis();

        Long queueJobMetricId = queueJobMetricService.recordEnqueued(
                com.aibe.team2.domain.admin.enums.QueueJobType.RESUME_ANALYSIS,
                "ANALYSIS_REPORT",
                event.reportId(),
                messageId,
                event.retryCount()
        );

        AnalysisMessage message = new AnalysisMessage(
                event.reportId(),
                event.resumeContent(),
                queueJobMetricId,
                event.retryCount()
        );
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            redisTemplate.opsForList().rightPush(QUEUE_KEY, jsonMessage);
            log.info("📦 [QueueProducer] DB 커밋 확인! Redis 대기열에 등록 완료 - Report ID: {}", event.reportId());
        } catch (JsonProcessingException e) {
            log.error("❌ [QueueProducer] 메시지 직렬화 실패 - Report ID: {}", event.reportId(), e);
        }
    }
}