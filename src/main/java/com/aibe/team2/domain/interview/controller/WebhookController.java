package com.aibe.team2.domain.interview.controller;

import com.aibe.team2.domain.interview.dto.RetellWebhookRequest;
import com.aibe.team2.domain.interview.enums.InterviewSessionStatus;
import com.aibe.team2.domain.interview.service.InterviewManager;
// import io.awspring.cloud.sqs.operations.SqsTemplate; // 🚀 SQS 임시 비활성화
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final InterviewManager interviewManager;
    // private final SqsTemplate sqsTemplate; // SQS 설정 전까지 임시 주석 처리

    @Value("${cloud.aws.sqs.webhook-queue-name:retell-webhook-queue}")
    private String webhookQueueName;

    // [FR-INT-04] Retell AI 종료 Webhook 수신 엔드포인트
    @PostMapping("/retell")
    public ResponseEntity<Void> handleRetellWebhook(@RequestBody RetellWebhookRequest request) {
        log.info("📨 Retell Webhook 수신 - Event: {}", request.getEvent());

        // SQS 연동 전이므로 발행 로직(sqsTemplate.send)을 주석 처리하고
        // 바로 DB를 업데이트하는 동기 처리 로직을 호출합니다.
        /*
        try {
            sqsTemplate.send(webhookQueueName, request);
            log.info("✅ SQS 큐({})에 Webhook 이벤트 발행 완료", webhookQueueName);
        } catch (Exception e) {
            log.error("❌ SQS 메시지 발행 실패: {}", e.getMessage());
            processWebhookSynchronously(request);
        }
        */

        // 당장 로컬에서 세션 종료 기능이 작동하도록 직접 호출
        processWebhookSynchronously(request);

        // Webhook 발송 서버(Retell)에게 즉시 200 OK 반환
        return ResponseEntity.ok().build();
    }

    // 실제 상태 변경 로직 (동기 처리)
    private void processWebhookSynchronously(RetellWebhookRequest request) {
        if ("call_ended".equals(request.getEvent()) || "call_analyzed".equals(request.getEvent())) {
            if (request.getCall() != null && request.getCall().getMetadata() != null) {
                Object sessionIdObj = request.getCall().getMetadata().get("session_id");
                if (sessionIdObj != null) {
                    try {
                        Long sessionId = Long.valueOf(sessionIdObj.toString());
                        log.info("🛑 음성 면접 세션 종료 동기 처리. 상태를 DONE으로 변경. SessionID: {}", sessionId);
                        interviewManager.advanceStatus(sessionId, InterviewSessionStatus.DONE);
                    } catch (NumberFormatException e) {
                        log.error("❌ Webhook 에러: 유효하지 않은 Session ID 형식. 값: {}", sessionIdObj);
                    } catch (IllegalArgumentException e) {
                        log.error("❌ Webhook 에러: 세션 상태 변경 실패 - {}", e.getMessage());
                    }
                } else {
                    log.warn("⚠️ Webhook 경고: metadata에 session_id가 없습니다.");
                }
            }
        }
    }
}