package com.aibe.team2.domain.resume.service;

import com.aibe.team2.domain.resume.service.AnalysisQueueProducer.AnalysisMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalysisQueueConsumer {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final AnalysisAsyncWorker asyncWorker;
    private final com.aibe.team2.domain.admin.service.QueueJobMetricService queueJobMetricService;

    private static final String QUEUE_KEY = "resume:analysis:queue";
    private static final int MAX_BATCH_SIZE = 3;

    @Scheduled(fixedDelay = 2000)
    public void consumeQueue() {

        for (int i = 0; i < MAX_BATCH_SIZE; i++) {
            Object jsonMessage = redisTemplate.opsForList().leftPop(QUEUE_KEY);

            if (jsonMessage == null) {
                break;
            }

            try {
                AnalysisMessage message = objectMapper.readValue(jsonMessage.toString(), AnalysisMessage.class);

                // 🔥 추가된 방어 로직: null 체크
                if (message.getQueueJobMetricId() != null) {
                    queueJobMetricService.markProcessing(message.getQueueJobMetricId());
                    log.info("🚀 [QueueConsumer] Redis 큐 추출 성공 - Report ID: {}, QueueMetricId: {}",
                            message.getReportId(), message.getQueueJobMetricId());
                } else {
                    log.warn("⚠️ [QueueConsumer] 과거 메시지 감지(MetricId 없음). 그냥 처리 진행 - Report ID: {}", message.getReportId());
                }

                asyncWorker.processAiAnalysisAsync(
                        message.getReportId(),
                        message.getResumeContent(),
                        message.getQueueJobMetricId() // null이어도 워커 내부에서 처리되도록 넘김
                );

            } catch (Exception e) {
                log.error("❌ [QueueConsumer] 큐 메시지 처리 중 오류 발생", e);
            }
        }
    }
}