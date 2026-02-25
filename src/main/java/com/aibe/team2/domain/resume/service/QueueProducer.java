package com.aibe.team2.domain.resume.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QueueProducer {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String ANALYSIS_QUEUE_TOPIC = "resume-analysis-queue";

    public void sendAnalysisRequest(Long memberId, Long resumeId, Long jobPostingId, Long reportId) {
        // 큐에 담을 메시지 객체 생성
        AnalysisMessage message = new AnalysisMessage(memberId, resumeId, jobPostingId, reportId);

        // Redis 큐(혹은 Pub/Sub)에 발행
        // (실무에서는 Redis List의 RightPush를 쓰거나, RabbitMQ/Kafka 등을 주로 사용합니다)
        redisTemplate.convertAndSend(ANALYSIS_QUEUE_TOPIC, message);

        log.info("Analysis request added to queue. Report ID: {}", reportId);
    }

    // 큐에 실어 보낼 메시지(DTO) 클래스
    @Data
    @AllArgsConstructor
    public static class AnalysisMessage {
        private Long memberId;
        private Long resumeId;
        private Long jobPostingId;
        private Long reportId; // 상태 업데이트를 위해 가장 중요한 ID
    }
}