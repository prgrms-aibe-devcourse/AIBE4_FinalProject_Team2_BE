package com.aibe.team2.domain.interview.service;

import com.aibe.team2.domain.interview.dto.RetellWebhookRequest;
import com.aibe.team2.domain.interview.enums.InterviewSessionStatus;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookSqsConsumer {

    private final InterviewManager interviewManager;

    // [NFR-PER-01] SQS Consumer (메시지 소비)
    // WebhookController에서 발행한 메시지를 비동기로 받아 세션 상태를 변경
    // @SqsListener("${cloud.aws.sqs.webhook-queue-name:retell-webhook-queue}") <- aws 키 설정 전까지 주석 처리
    public void consumeRetellWebhook(RetellWebhookRequest request) {
        log.info("📥 SQS Consumer: Retell Webhook 메시지 수신 완료 - Event: {}", request.getEvent());

        if ("call_ended".equals(request.getEvent()) || "call_analyzed".equals(request.getEvent())) {
            if (request.getCall() != null && request.getCall().getMetadata() != null) {
                Object sessionIdObj = request.getCall().getMetadata().get("session_id");

                if (sessionIdObj != null) {
                    try {
                        Long sessionId = Long.valueOf(sessionIdObj.toString());
                        log.info("🛑 SQS 비동기 처리: 세션 상태를 DONE으로 업데이트합니다. SessionID: {}", sessionId);

                        // DB 업데이트 실행
                        interviewManager.advanceStatus(sessionId, InterviewSessionStatus.DONE);

                    } catch (NumberFormatException e) {
                        log.error("❌ SQS 처리 에러: 유효하지 않은 Session ID 형식. 값: {}", sessionIdObj);
                    } catch (IllegalArgumentException e) {
                        log.error("❌ SQS 처리 에러: 세션 상태 변경 실패 - {}", e.getMessage());
                    }
                }
            }
        }
    }
}