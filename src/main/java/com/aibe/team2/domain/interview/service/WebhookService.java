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

    // Magic String 지양 및 이벤트 문자열 상수화
    private static final String EVENT_CALL_ENDED = "call_ended";
    private static final String EVENT_CALL_ANALYZED = "call_analyzed";

    public void processRetellWebhook(RetellWebhookRequest request) {
        String event = request.getEvent();

        if (!EVENT_CALL_ENDED.equals(event) && !EVENT_CALL_ANALYZED.equals(event)) {
            return;
        }

        // 필수 객체(Call, Metadata)가 누락된 경우 즉시 리턴
        if (request.getCall() == null || request.getCall().getMetadata() == null) {
            log.warn("⚠️ Webhook 경고: call 또는 metadata 정보가 누락되었습니다.");
            return;
        }

        // Session ID가 누락된 경우 즉시 리턴
        Object sessionIdObj = request.getCall().getMetadata().get("session_id");
        if (sessionIdObj == null) {
            log.warn("⚠️ Webhook 경고: metadata에 session_id가 없습니다.");
            return;
        }

        // 핵심 비즈니스 로직 실행 (모든 검증을 통과한 안전한 상태)
        try {
            Long sessionId = Long.valueOf(sessionIdObj.toString());
            log.info("🛑 Webhook 처리: 상태를 DONE으로 변경. SessionID: {}", sessionId);
            interviewManager.advanceStatus(sessionId, InterviewSessionStatus.DONE);

        } catch (NumberFormatException e) {
            log.error("❌ Webhook 에러: 유효하지 않은 Session ID 형식. 값: {}", sessionIdObj);
        } catch (IllegalArgumentException e) {
            log.error("❌ Webhook 에러: 세션 상태 변경 실패 - {}", e.getMessage());
        }
    }
}