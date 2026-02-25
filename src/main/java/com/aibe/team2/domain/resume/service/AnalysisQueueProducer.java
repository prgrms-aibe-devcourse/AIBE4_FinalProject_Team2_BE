package com.aibe.team2.domain.resume.service;

import com.aibe.team2.domain.resume.dto.ResumeAnalysisEvent;
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

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AnalysisMessage {
        private Long reportId;
        private String resumeContent;
        private String fullJobDescription;
    }

    // 이전 트랜잭션이 완벽하게 Commit(완료)된 직후(AFTER_COMMIT)에만 이 메서드가 실행됩니다!
    // 만약 DB 저장 중 에러가 나서 롤백되면 이 메서드는 아예 실행되지 않으므로 안전합니다.
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleResumeAnalysisEvent(ResumeAnalysisEvent event) {
        AnalysisMessage message = new AnalysisMessage(
                event.reportId(),
                event.resumeContent(),
                event.fullJobDescription()
        );

        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            redisTemplate.opsForList().rightPush(QUEUE_KEY, jsonMessage);
            log.info("[QueueProducer] DB 커밋 확인! 큐 대기열에 분석 요청 안전하게 등록 완료 - Report ID: {}", event.reportId());
        } catch (JsonProcessingException e) {
            log.error("[QueueProducer] 메시지 직렬화 실패 - Report ID: {}", event.reportId(), e);
        }
    }
}