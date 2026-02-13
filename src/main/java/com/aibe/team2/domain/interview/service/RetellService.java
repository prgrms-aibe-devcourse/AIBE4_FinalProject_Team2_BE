package com.aibe.team2.domain.interview.service;

import com.aibe.team2.domain.interview.dto.VoiceSessionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class RetellService {

    @Value("${retell.api.key}")
    private String apiKey;

    @Value("${retell.agent.id}")
    private String agentId;

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
}