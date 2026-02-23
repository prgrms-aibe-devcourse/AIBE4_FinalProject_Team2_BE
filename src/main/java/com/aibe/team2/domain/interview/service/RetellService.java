package com.aibe.team2.domain.interview.service;

import com.aibe.team2.domain.interview.dto.VoiceSessionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class RetellService {

    @Value("${retell.api.key}")
    private String apiKey;

    @Value("${retell.agent.id}")
    private String agentId;

    /**
     * [기존] Retell 음성 면접 세션 생성
     */
    public VoiceSessionResponse createVoiceCall(Long sessionId) {
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://api.retellai.com/v2/create-web-call";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = Map.of(
                "agent_id", agentId,
                "metadata", Map.of("session_id", sessionId)
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

        String accessToken = (String) response.getBody().get("access_token");
        return new VoiceSessionResponse(accessToken);
    }

    /**
     * Retell AI를 사용한 텍스트 대화 응답 생성
     */
    public String getChatResponse(String userMessage) {
        RestTemplate restTemplate = new RestTemplate();
        // Retell의 LLM 응답 API 엔드포인트
        // Note: 실제 사용 시 에이전트에 연결된 'llm_id'가 필요할 수 있습니다.
        // 여기서는 일반적인 Retell LLM 인터랙션 구조를 따릅니다.
        String url = "https://api.retellai.com/v2/create-chat";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = Map.of(
                "user_message", userMessage,
                "chat_history", List.of()
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
            // Retell 응답에서 텍스트 내용 추출
            return (String) response.getBody().get("content");
        } catch (Exception e) {
            return "[Retell 에러] 답변을 생성할 수 없습니다: " + e.getMessage();
        }
    }
}