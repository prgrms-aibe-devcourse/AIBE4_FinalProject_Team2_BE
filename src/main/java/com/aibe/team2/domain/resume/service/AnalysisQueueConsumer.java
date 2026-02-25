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

        // 큐에 쌓인 일감이 없을 때까지 연속으로 전부 꺼내서 처리합니다.
        while (true) {
            // Redis List의 왼쪽(Left)에서 데이터를 하나 뽑아옵니다. (없으면 null 반환)
            Object jsonMessage = redisTemplate.opsForList().leftPop(QUEUE_KEY);

            // 큐가 비어있으면 반복문을 즉시 탈출하고, 다음 스케줄링(1초 뒤)을 기다립니다.
            if (jsonMessage == null) {
                break;
            }

            try {
                // JSON 텍스트를 다시 Java 객체로 변환
                AnalysisQueueProducer.AnalysisMessage message =
                        objectMapper.readValue(jsonMessage.toString(), AnalysisQueueProducer.AnalysisMessage.class);

                log.info("[QueueConsumer] 큐에서 작업 추출 성공! AI 분석 비동기 위임 - Report ID: {}", message.getReportId());

                // 비동기 워커 실행!
                // 💡 주의: asyncWorker의 메서드는 @Async가 붙어있으므로, 여기서 결과를 기다리지 않고 백그라운드 스레드 풀에 던진 뒤 즉시 다음 루프로 넘어갑니다.
                asyncWorker.processAiAnalysisAsync(
                        message.getReportId(),
                        message.getResumeContent(),
                        message.getFullJobDescription()
                );
            } catch (Exception e) {
                // 특정 메시지 파싱에 실패하더라도 반복문(while)이 멈추지 않고 다음 메시지를 꺼내도록 catch로 잡습니다.
                log.error("[QueueConsumer] 큐 메시지 처리 중 오류 발생", e);
            }
        }
    }
}