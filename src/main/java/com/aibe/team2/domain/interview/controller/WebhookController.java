package com.aibe.team2.domain.interview.controller;

import com.aibe.team2.domain.interview.dto.RetellWebhookRequest;
import com.aibe.team2.domain.interview.service.WebhookService;
// import io.awspring.cloud.sqs.operations.SqsTemplate; // SQS 설정 전까지 임시 주석 처리
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Slf4j
// @RestController 도메인 배포 및 Webhook 연동 전까지 임시 비활성화
// @RequestMapping("/api/webhooks") 도메인 배포 및 Webhook 연동 전까지 임시 비활성화
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookService webhookService;
    private final ObjectMapper objectMapper; // JSON 파싱용 의존성
    // private final SqsTemplate sqsTemplate; // SQS 설정 전까지 임시 주석 처리

    @Value("${cloud.aws.sqs.webhook-queue-name:retell-webhook-queue}")
    private String webhookQueueName;

    @Value("${retell.webhook.secret:your-default-webhook-secret}") // Webhook 검증용 시크릿
    private String webhookSecret;

    // [FR-INT-04] Retell AI 종료 Webhook 수신 엔드포인트
    @PostMapping("/retell")
    public ResponseEntity<Void> handleRetellWebhook(
            @RequestHeader(value = "X-Retell-Signature", required = false) String signature,
            @RequestBody String rawBody) {

        log.info("📨 Retell Webhook 수신 요청 확인");

        // Webhook 서명(Signature) 검증을 통한 위조 요청(Forged Request) 방어
        if (!isValidSignature(rawBody, signature)) {
            log.warn("🚨 보안 위협: 유효하지 않은 Webhook 서명입니다. 요청이 거부되었습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401 에러 반환
        }

        try {
            // 서명 검증 통과 후, 안전하게 Raw String을 DTO로 파싱
            RetellWebhookRequest request = objectMapper.readValue(rawBody, RetellWebhookRequest.class);
            log.info("✅ 서명 검증 통과 및 Payload 파싱 성공 - Event: {}", request.getEvent());

            // 당장 로컬에서 세션 종료 기능이 작동하도록 직접 호출 (SQS 주석 처리 상태)
            processWebhookSynchronously(request);

        } catch (JsonProcessingException e) {
            log.error("❌ Webhook Payload 파싱 에러: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }

        // Webhook 발송 서버(Retell)에게 즉시 200 OK 반환
        return ResponseEntity.ok().build();
    }

    // 실제 상태 변경 로직 (동기 처리) - WebhookService로 위임
    private void processWebhookSynchronously(RetellWebhookRequest request) {
        webhookService.processRetellWebhook(request);
    }

    // HMAC-SHA256 알고리즘을 사용한 Webhook 서명 검증 로직
    private boolean isValidSignature(String payload, String signature) {
        if (signature == null || webhookSecret == null || webhookSecret.isBlank()) {
            return false;
        }

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);

            // 전달받은 Raw Body 데이터로 Hash 연산
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));

            // 생성된 Hash 값을 Hex(16진수) 문자열로 변환
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            // 헤더로 전달받은 서명(signature)과 서버에서 직접 계산한 서명(hexString) 대조
            return hexString.toString().equals(signature);

        } catch (Exception e) {
            log.error("❌ 서명 검증 로직 수행 중 에러 발생: {}", e.getMessage());
            return false;
        }
    }
}