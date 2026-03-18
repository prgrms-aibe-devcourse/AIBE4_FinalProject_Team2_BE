package com.aibe.team2.domain.interview.service;

import com.aibe.team2.domain.interview.dto.RetellWebhookRequest;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookSqsConsumer {

    private final WebhookService webhookService;

    // [NFR-PER-01] SQS Consumer (메시지 소비) WebhookController에서 발행한 메시지를 비동기로 받아 처리
    @SqsListener("${cloud.aws.sqs.webhook-queue-name:retell-webhook-queue}") // SQS 연동 시 주석 해제
    public void consumeRetellWebhook(RetellWebhookRequest request) {
        log.info("📥 SQS Consumer: Retell Webhook 메시지 수신 완료 - Event: {}", request.getEvent());

        // 리뷰 반영: 직접 처리하지 않고 공통 서비스로 위임 (DRY 원칙)
        webhookService.processRetellWebhook(request);
    }
}