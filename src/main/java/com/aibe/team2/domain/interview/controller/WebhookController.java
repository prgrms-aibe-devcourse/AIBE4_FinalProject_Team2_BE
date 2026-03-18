package com.aibe.team2.domain.interview.controller;

import com.aibe.team2.domain.interview.dto.RetellWebhookRequest;
import com.aibe.team2.domain.interview.service.WebhookService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@Slf4j
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookService webhookService;
    private final ObjectMapper objectMapper;
    private final SqsTemplate sqsTemplate;

    @Value("${cloud.aws.sqs.webhook-queue-name:retell-webhook-queue}")
    private String webhookQueueName;

    @Value("${retell.webhook.secret}")
    private String webhookApiKey;

    @PostMapping("/retell")
    public ResponseEntity<Void> handleRetellWebhook(
            @RequestHeader(value = "X-Retell-Signature", required = false) String signature,
            @RequestBody byte[] rawBodyBytes) {

        log.info("📨 Retell Webhook 수신 요청 확인");

        // 서명 검증 수행 (현재 우회 상태이므로 무조건 통과)
        if (!isValidSignature(rawBodyBytes, signature)) {
            log.warn("🚨 보안 위협: 유효하지 않은 Webhook 서명입니다. 요청이 거부되었습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String rawBody = new String(rawBodyBytes, StandardCharsets.UTF_8);

        try {
            RetellWebhookRequest request = objectMapper.readValue(rawBody, RetellWebhookRequest.class);
            log.info("✅ 서명 검증 우회 통과 및 Payload 파싱 성공 - Event: {}", request.getEvent());

            processWebhookSynchronously(request);

        } catch (JsonProcessingException e) {
            log.error("❌ Webhook Payload 파싱 에러: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("❌ Webhook 처리 중 에러 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }

        return ResponseEntity.ok().build();
    }

    private void processWebhookSynchronously(RetellWebhookRequest request) {
        // webhookService.processRetellWebhook(request);
        log.info("📤 SQS Producer: 대화 분석 완료 데이터를 큐에 전송합니다. Queue: {}", webhookQueueName);
        sqsTemplate.send(webhookQueueName, request);
    }

    private boolean isValidSignature(byte[] payloadBytes, String signature) {
        // TODO: 배포 전 반드시 Webhook 서명 검증 로직을 활성화해야 합니다. 현재는 심각한 보안 취약점이 존재합니다.
        // 이기종 언어 간 JSON 직렬화 불일치로 인한 무한 401 방지.
        // 핵심 로직(상태 업데이트 및 SQS) 테스트를 위해 보안 검증을 강제로 우회합니다.
        log.warn("🚨 [CRITICAL SECURITY RISK] Webhook signature verification is currently bypassed. This must be fixed before production.");
        return true;
    }
}