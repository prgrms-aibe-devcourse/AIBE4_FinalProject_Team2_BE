package com.aibe.team2.domain.resume.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SimilarityEngine {

    private final WebClient.Builder webClientBuilder;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    // 텍스트 생성용 모델이 아닌 임베딩(Embedding) 전용 모델 URL을 사용합니다.
    private static final String EMBEDDING_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/text-embedding-004:embedContent";

    /**
     * 채용 공고(JD)와 자소서의 코사인 유사도를 계산하여 100점 만점으로 반환합니다.
     */
    public int calculateCosineSimilarityScore(String resumeContent, String jobDescription) {
        try {
            // 1. 각각의 텍스트를 벡터로 변환
            List<Double> resumeVector = getEmbeddingVector(resumeContent);
            List<Double> jdVector = getEmbeddingVector(jobDescription);

            // 2. 코사인 유사도 계산 (0.0 ~ 1.0)
            double similarity = computeCosineSimilarity(resumeVector, jdVector);

            // 3. 100점 만점으로 환산 (음수가 나올 수 있으므로 0 미만은 0처리)
            int finalScore = (int) (Math.max(similarity, 0.0) * 100);

            log.info("[SimilarityEngine] 코사인 유사도: {}, 최종 점수: {}점", similarity, finalScore);
            return finalScore;

        } catch (Exception e) {
            log.error("[SimilarityEngine] 유사도 계산 중 오류 발생. 기본 점수 반환", e);
            return 50; // API 통신 장애 등 예외 발생 시 프로세스 중단을 막기 위한 기본 점수
        }
    }

    /**
     * Gemini Embedding API를 호출하여 텍스트를 벡터(List<Double>)로 변환합니다.
     */
    private List<Double> getEmbeddingVector(String text) {
        Map<String, Object> requestBody = Map.of(
                "model", "models/text-embedding-004",
                "content", Map.of("parts", List.of(Map.of("text", text)))
        );

        WebClient webClient = webClientBuilder.baseUrl(EMBEDDING_API_URL).build();

        JsonNode responseNode = webClient.post()
                .uri(uriBuilder -> uriBuilder.queryParam("key", geminiApiKey).build())
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(30))
                .block();

        if (responseNode == null || !responseNode.has("embedding")) {
            throw new RuntimeException("임베딩 결과를 파싱할 수 없습니다.");
        }

        // JSON 배열을 List<Double>로 변환
        List<Double> vector = new ArrayList<>();
        responseNode.get("embedding").get("values").forEach(node -> vector.add(node.asDouble()));

        return vector;
    }

    /**
     * 두 벡터 간의 코사인 유사도를 계산하는 수학 공식 (A · B / ||A|| ||B||)
     */
    private double computeCosineSimilarity(List<Double> vectorA, List<Double> vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        int minLength = Math.min(vectorA.size(), vectorB.size());

        for (int i = 0; i < minLength; i++) {
            dotProduct += vectorA.get(i) * vectorB.get(i);
            normA += Math.pow(vectorA.get(i), 2);
            normB += Math.pow(vectorB.get(i), 2);
        }

        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}