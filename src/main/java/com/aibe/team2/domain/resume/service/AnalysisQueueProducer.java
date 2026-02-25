package com.aibe.team2.domain.resume.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalysisQueueProducer {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String QUEUE_KEY = "resume:analysis:queue"; // Redis에 저장될 큐 이름

    // 큐에 넣을 메시지 구조체(DTO)
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AnalysisMessage {
        private Long reportId;
        private String resumeContent;
        private String fullJobDescription;
    }

    public void sendAnalysisRequest(Long reportId, String resumeContent, String fullJobDescription) {
        AnalysisMessage message = new AnalysisMessage(reportId, resumeContent, fullJobDescription);
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            // Redis List의 오른쪽(Right)에 데이터를 밀어 넣습니다. (FIFO 구조)
            redisTemplate.opsForList().rightPush(QUEUE_KEY, jsonMessage);
            log.info("[QueueProducer] 큐 대기열에 분석 요청 등록 완료 - Report ID: {}", reportId);
        } catch (JsonProcessingException e) {
            log.error("[QueueProducer] 메시지 직렬화 실패 - Report ID: {}", reportId, e);
        }
    }
}