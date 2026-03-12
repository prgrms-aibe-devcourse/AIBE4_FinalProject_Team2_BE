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

    // 구글 최신 공식 임베딩 모델
    private static final String EMBEDDING_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/text-embedding-004:embedContent";

    /**
     * 텍스트를 입력받아 pgvector DB 컬럼에 바로 저장할 수 있는 float[] 배열(768차원)로 반환합니다.
     */
    public float[] getEmbeddingAsFloatArray(String text) {
        // 빈 텍스트가 들어오면 null 반환 (DB에도 null 저장)
        if (text == null || text.isBlank()) return null;

        Map<String, Object> requestBody = Map.of(
                "model", "models/text-embedding-004",
                "content", Map.of("parts", List.of(Map.of("text", text)))
        );

        WebClient webClient = webClientBuilder.build();
        String fullUrl = EMBEDDING_API_URL + "?key=" + geminiApiKey;

        try {
            JsonNode responseNode = webClient.post()
                    .uri(URI.create(fullUrl))
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            if (responseNode == null || !responseNode.has("embedding")) {
                throw new RuntimeException("임베딩 결과를 파싱할 수 없습니다.");
            }

            // ★ 핵심 변경 포인트: List<Double> 대신 float[] 배열로 변환
            JsonNode valuesNode = responseNode.get("embedding").get("values");
            float[] vector = new float[valuesNode.size()];

            for (int i = 0; i < valuesNode.size(); i++) {
                vector[i] = (float) valuesNode.get(i).asDouble();
            }

            return vector;

        } catch (WebClientResponseException e) {
            log.error("[SimilarityEngine] 임베딩 API 호출 실패 - 상태코드: {}, 에러: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return null; // 에러 시 파이프라인 중단 방지를 위해 null 반환
        } catch (Exception e) {
            log.error("[SimilarityEngine] 임베딩 변환 중 예기치 않은 오류 발생", e);
            return null;
        }
    }
}