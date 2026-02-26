package com.aibe.team2.domain.resume.service;

import com.aibe.team2.domain.resume.dto.ResumeAnalysisEvent;
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

    // 🌟 [핵심] 한 번 스케줄러가 동작할 때 최대 처리할 메시지 개수 제한 (Batch Size)
    private static final int MAX_BATCH_SIZE = 50;

    @Scheduled(fixedDelay = 1000)
    public void consumeQueue() {

        // 🌟 무한 루프(while true) 대신 지정된 개수(MAX_BATCH_SIZE)만큼만 반복하는 for문 사용
        for (int i = 0; i < MAX_BATCH_SIZE; i++) {
            Object jsonMessage = redisTemplate.opsForList().leftPop(QUEUE_KEY);

            // 큐가 비어있으면 즉시 탈출 (50번을 다 돌기 전이라도 멈춤)
            if (jsonMessage == null) {
                break;
            }

            try {
                ResumeAnalysisEvent event;
                event = objectMapper.readValue(jsonMessage.toString(), ResumeAnalysisEvent.class);
                log.info("[QueueConsumer] 큐에서 작업 추출 성공! AI 분석 비동기 위임 - Report ID: {}", event.reportId());
                // 비동기 워커 실행!
                // 💡 주의: asyncWorker의 메서드는 @Async가 붙어있으므로, 여기서 결과를 기다리지 않고 백그라운드 스레드 풀에 던진 뒤 즉시 다음 루프로 넘어갑니다.
                asyncWorker.processAiAnalysisAsync(
                        event.reportId(),
                        event.resumeContent(),
                        event.fullJobDescription()
                );
            } catch (Exception e) {
                log.error("[QueueConsumer] 큐 메시지 처리 중 오류 발생", e);
            }
        }
    }
}