package com.aibe.team2.domain.interview.service;

import com.aibe.team2.domain.interview.dto.RetellWebhookRequest;
import com.aibe.team2.domain.interview.enums.InterviewSessionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    private final InterviewManager interviewManager;

    private static final String EVENT_CALL_ENDED = "call_ended";
    private static final String EVENT_CALL_ANALYZED = "call_analyzed";

    public void processRetellWebhook(RetellWebhookRequest request) {
        String event = request.getEvent();

        // 처리 대상 이벤트가 아니면 무시
        if (!EVENT_CALL_ENDED.equals(event) && !EVENT_CALL_ANALYZED.equals(event)) {
            return;
        }

        if (request.getCall() == null || request.getCall().getMetadata() == null) {
            log.warn("⚠️ Webhook 경고: call 또는 metadata 정보가 누락되었습니다.");
            return;
        }

        Object sessionIdObj = request.getCall().getMetadata().get("session_id");
        if (sessionIdObj == null) {
            log.warn("⚠️ Webhook 경고: metadata에 session_id가 없습니다.");
            return;
        }

        try {
            Long sessionId = Long.valueOf(sessionIdObj.toString());

            // [보완] call_ended나 call_analyzed 중 먼저 도착하는 이벤트로 세션을 종료 처리합니다.
            log.info("🛑 Webhook 처리: 세션 상태를 DONE으로 변경 시도. SessionID: {}, Event: {}", sessionId, event);
            interviewManager.advanceStatus(sessionId, InterviewSessionStatus.DONE);

        } catch (Exception e) {
            log.error("❌ Webhook 처리 중 예외 발생", e);
        }
    }
}