package com.aibe.team2.domain.interview.service;

import com.aibe.team2.domain.interview.dto.InterviewRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class GeminiService {

    private final WebClient webClient;
    private final ResourceLoader resourceLoader; // 🚀 파일 로드를 위해 추가
    private final String apiKey;
    private final String baseUrl;

    public GeminiService(WebClient.Builder webClientBuilder,
                         ResourceLoader resourceLoader,
                         @Value("${gemini.api.key}") String apiKey,
                         @Value("${gemini.api.url}") String baseUrl) {
        this.apiKey = apiKey.trim();
        this.baseUrl = baseUrl.trim();
        this.resourceLoader = resourceLoader;
        this.webClient = webClientBuilder.build();
    }

    public Flux<String> streamQuestion(String sessionId, InterviewRequestDto request) {
        // 1. 기존 성공 로직: URL 도메인 추출
        String rootUrl = baseUrl.split("/v1")[0];
        if (rootUrl.endsWith("/")) {
            rootUrl = rootUrl.substring(0, rootUrl.length() - 1);
        }

        // 2. 모델 설정: 404가 나지 않는 'gemini-flash-latest'를 기본값으로 사용
        String model = (request.getModelVariant() != null && !request.getModelVariant().isEmpty())
                ? request.getModelVariant() : "gemini-flash-latest";

        // 3. 🚀 핵심: 정상 작동이 확인된 URL 구조 (?alt=sse 포함)
        String fullUrl = String.format("%s/v1beta/models/%s:streamGenerateContent?alt=sse", rootUrl, model);

        // 4. 페르소나 및 제약사항 파일 로드 로직 통합
        String personaPrompt = loadPromptFile(request.getPersonaType());
        String constraints = loadPromptFile("constraints");
        String finalPrompt = String.format("%s\n\n%s\n\n[Candidate Answer]\n%s",
                personaPrompt, constraints, request.getMessage());

        log.info("[Gemini-Success] Session: {}, Model: {}, URL: {}", sessionId, model, fullUrl);

        return webClient.post()
                .uri(URI.create(fullUrl))
                // 🛡️ 기존 성공 방식인 헤더 인증 사용
                .header("x-goog-api-key", apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(Map.of("contents", List.of(
                        Map.of("role", "user",
                                "parts", List.of(Map.of("text", finalPrompt)))
                )))
                .retrieve()
                .bodyToFlux(String.class)
                .doOnError(e -> log.error("=== 🚨 Gemini API 호출 에러: {} ===", e.getMessage()));
    }

    // 파일 로드 헬퍼 메서드
    private String loadPromptFile(String fileName) {
        try {
            Resource resource = resourceLoader.getResource("classpath:prompts/" + fileName + ".txt");
            if (!resource.exists()) resource = resourceLoader.getResource("classpath:prompts/SENIOR.txt");
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "당신은 면접관입니다.";
        }
    }
}