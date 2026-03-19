package com.aibe.team2.domain.interview.service;

import com.aibe.team2.domain.interview.dto.RetellWebhookRequest;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookSqsConsumer {

    private final WebhookService webhookService;
    private final InterviewRecordService interviewRecordService;
    private final InterviewAnalysisService interviewAnalysisService; // 분석 서비스 주입

    // SQS로부터 Retell Webhook 메시지를 수신하여 비동기로 처리
    @SqsListener("${cloud.aws.sqs.webhook-queue-name:retell-webhook-queue}")
    public void consumeRetellWebhook(RetellWebhookRequest request) {
        log.info("📥 SQS Consumer: Retell Webhook 메시지 수신 완료 - Event: {}", request.getEvent());

        // 1. 공통 상태 변경 로직 수행 (CREATED -> DONE)
        webhookService.processRetellWebhook(request);

        // 2. 대화 분석 완료 이벤트인 경우 기록 저장 및 AI 분석 트리거
        if ("call_analyzed".equals(request.getEvent())) {
            // 대화 기록 저장 (InterviewRecord 생성)
            interviewRecordService.saveInterviewRecord(request);

            // 세션 ID 추출 후 AI 분석(점수/피드백) 시작
            Long sessionId = extractSessionId(request);
            if (sessionId != null) {
                // [핵심] 비동기로 AI 피드백 생성을 시작합니다.
                interviewAnalysisService.analyzeSession(sessionId);
            }
        }
    }

    private Long extractSessionId(RetellWebhookRequest request) {
        try {
            Map<String, Object> metadata = request.getCall().getMetadata();
            if (metadata != null && metadata.containsKey("session_id")) {
                return Long.valueOf(metadata.get("session_id").toString());
            }
        } catch (Exception e) {
            log.error("❌ Session ID 추출 실패: {}", e.getMessage());
        }
        return null;
    }
}