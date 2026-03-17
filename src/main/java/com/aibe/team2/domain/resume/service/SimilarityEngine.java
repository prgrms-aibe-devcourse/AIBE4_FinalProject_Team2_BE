package com.aibe.team2.domain.resume.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SimilarityEngine {

    private final WebClient.Builder webClientBuilder;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private static final String EMBEDDING_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-embedding-001:embedContent";

    public float[] getEmbeddingAsFloatArray(String text) {
        if (text == null || text.isBlank()) return null;

        Map<String, Object> requestBody = Map.of(
                "model", "models/text-embedding-004",
                "content", Map.of("parts", List.of(Map.of("text", text)))
        );

        WebClient webClient = webClientBuilder.build();

        try {
            JsonNode responseNode = webClient.post()
                    .uri(URI.create(EMBEDDING_API_URL))
                    .header("x-goog-api-key", geminiApiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            if (responseNode == null || !responseNode.has("embedding")) {
                throw new RuntimeException("임베딩 결과를 파싱할 수 없습니다.");
            }

            JsonNode valuesNode = responseNode.get("embedding").get("values");
            float[] vector = new float[valuesNode.size()];

            for (int i = 0; i < valuesNode.size(); i++) {
                vector[i] = (float) valuesNode.get(i).asDouble();
            }

            return vector;

        } catch (WebClientResponseException e) {
            log.error("[SimilarityEngine] 임베딩 API 호출 실패 - 상태코드: {}, 에러: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            log.error("[SimilarityEngine] 임베딩 변환 중 예기치 않은 오류 발생", e);
            return null;
        }
    }
}