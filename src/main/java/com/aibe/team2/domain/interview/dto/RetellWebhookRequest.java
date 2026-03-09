package com.aibe.team2.domain.interview.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class RetellWebhookRequest {

    // 발생한 이벤트 타입 (예: "call_started", "call_ended", "call_analyzed")
    private String event;

    // 통화 상세 정보
    private CallDetail call;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class CallDetail {
        @JsonProperty("call_id")
        private String callId;

        // 생성 시 넘겨주었던 metadata (여기에 session_id가 포함됨)
        private Map<String, Object> metadata;
    }
}