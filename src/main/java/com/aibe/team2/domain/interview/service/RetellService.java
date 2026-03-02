package com.aibe.team2.domain.interview.service;

import com.aibe.team2.domain.interview.dto.VoiceSessionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 로그 추가
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RetellService {

    private final RestTemplate restTemplate;

    @Value("${retell.api-key}")
    private String apiKey;

    @Value("${retell.agent-id}")
    private String agentId;

    public VoiceSessionResponse createVoiceCall(Long sessionId) {
        String url = "https://api.retellai.com/v2/create-web-call";

        log.info("Retell 음성 세션 생성 시작 - SessionID: {}, AgentID: {}", sessionId, agentId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // 요청 바디 구성
        Map<String, Object> body = Map.of(
                "agent_id", agentId,
                "metadata", Map.of("session_id", sessionId)
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getBody() != null && response.getBody().containsKey("access_token")) {
                String accessToken = (String) response.getBody().get("access_token");
                log.info("Retell 세션 생성 성공");
                return new VoiceSessionResponse(accessToken);
            }

            throw new RuntimeException("Retell 응답에서 access_token을 찾을 수 없습니다.");

        } catch (Exception e) {
            log.error("Retell API 호출 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException("음성 면접 세션 생성에 실패했습니다.");
        }
    }
}