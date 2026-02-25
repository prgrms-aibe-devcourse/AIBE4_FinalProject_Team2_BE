package com.aibe.team2.domain.resume.service;

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
    private final ResumeAnalysisAsyncWorker asyncWorker;
    private static final String QUEUE_KEY = "resume:analysis:queue";

    // 1초(1000ms)마다 백그라운드에서 큐를 확인합니다.
    @Scheduled(fixedDelay = 1000)
    public void consumeQueue() {
        // Redis List의 왼쪽(Left)에서 데이터를 하나 뽑아옵니다. (없으면 null 반환)
        Object jsonMessage = redisTemplate.opsForList().leftPop(QUEUE_KEY);

        if (jsonMessage != null) {
            try {
                // JSON 텍스트를 다시 Java 객체로 변환
                AnalysisQueueProducer.AnalysisMessage message =
                        objectMapper.readValue(jsonMessage.toString(), AnalysisQueueProducer.AnalysisMessage.class);

                log.info("[QueueConsumer] 큐에서 작업 추출 성공! AI 분석 시작 - Report ID: {}", message.getReportId());

                // 비동기 워커 실행!
                asyncWorker.processAiAnalysisAsync(
                        message.getReportId(),
                        message.getResumeContent(),
                        message.getFullJobDescription()
                );
            } catch (Exception e) {
                log.error("[QueueConsumer] 큐 메시지 처리 중 오류 발생", e);
            }
        }
    }
}