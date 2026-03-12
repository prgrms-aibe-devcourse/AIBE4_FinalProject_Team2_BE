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

    private static final String QUEUE_KEY = "resume:analysis:queue";
    private static final int MAX_BATCH_SIZE = 3;
    @Scheduled(fixedDelay = 2000) // 2초마다 큐 확인
    public void consumeQueue() {

        for (int i = 0; i < MAX_BATCH_SIZE; i++) {
            Object jsonMessage = redisTemplate.opsForList().leftPop(QUEUE_KEY);

            // 큐가 비어있으면 즉시 스케줄러 루프 탈출
            if (jsonMessage == null) {
                break;
            }

            try {
                AnalysisMessage message = objectMapper.readValue(jsonMessage.toString(), AnalysisMessage.class);
                log.info("🚀 [QueueConsumer] Redis 큐에서 작업 추출 성공! Worker로 위임 - Report ID: {}", message.getReportId());

                // 꺼낸 작업을 @Async 워커 스레드로 넘김
                asyncWorker.processAiAnalysisAsync(message.getReportId(), message.getResumeContent());

            } catch (Exception e) {
                log.error("❌ [QueueConsumer] 큐 메시지 처리 중 오류 발생", e);
            }
        }
    }
}