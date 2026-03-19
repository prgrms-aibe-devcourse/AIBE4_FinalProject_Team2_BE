package com.aibe.team2.domain.log.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogDTO {
    private String timestamp;
    private String requestId;
    private String method;
    private String uri;
    private int status;
    private String clientIp;
    private String userEmail;
    private long elapsedTime;
    private String requestBody;
    private String responseBody;

    // JSON 문자열로 변환 (ObjectMapper 사용)
    public String toJson(ObjectMapper objectMapper) throws JsonProcessingException {
        return objectMapper.writeValueAsString(this);
    }
}