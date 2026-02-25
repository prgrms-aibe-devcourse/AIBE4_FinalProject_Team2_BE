package com.aibe.team2.domain.resume.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResumeAnalysisQueueProducer {

    private final StringRedisTemplate redisTemplate; // 문자열(JSON) 직렬화에 최적화된 템플릿
    private final ObjectMapper objectMapper;
    private static final String QUEUE_KEY = "resume:analysis:queue";

    public void enqueueAnalysisTask(Long reportId, String resumeContent, String fullJobDescription) {
        try {
            // 작업 데이터를 객체로 묶고 JSON으로 변환
            ResumeAnalysisTask task = new ResumeAnalysisTask(reportId, resumeContent, fullJobDescription);
            String taskJson = objectMapper.writeValueAsString(task);

            // Redis 리스트의 오른쪽(Right)에 차곡차곡 삽입 (Queue 역할)
            redisTemplate.opsForList().rightPush(QUEUE_KEY, taskJson);
            log.info("[QueueProducer] 분석 요청 Redis 큐에 추가됨 - Report ID: {}", reportId);

        } catch (Exception e) {
            log.error("[QueueProducer] Redis 큐 전송 실패 - Report ID: {}", reportId, e);
        }
    }

    // 큐에 담을 내부 DTO 레코드
    public record ResumeAnalysisTask(Long reportId, String resumeContent, String fullJobDescription) {}
}