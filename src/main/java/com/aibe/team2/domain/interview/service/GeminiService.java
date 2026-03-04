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
    private final ResourceLoader resourceLoader;
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
        String rootUrl = baseUrl.split("/v1")[0];
        if (rootUrl.endsWith("/")) rootUrl = rootUrl.substring(0, rootUrl.length() - 1);

        String model = (request.getModelVariant() != null && !request.getModelVariant().isEmpty())
                ? request.getModelVariant() : "gemini-flash-latest";

        String fullUrl = String.format("%s/v1beta/models/%s:streamGenerateContent?alt=sse", rootUrl, model);

        // interviewMode.name()을 파일명으로 사용하여 분위기 프롬프트 로드 (NORMAL, FOLLOW_UP, STRESS)
        String atmospherePrompt = loadPromptFile(request.getInterviewMode().name());
        String constraints = loadPromptFile("constraints");
        String finalPrompt = String.format("%s\n\n%s\n\n[Candidate Answer]\n%s",
                atmospherePrompt, constraints, request.getMessage());

        log.info("[Gemini-Success] Session: {}, Mode: {}", sessionId, request.getInterviewMode());

        return webClient.post()
                .uri(URI.create(fullUrl))
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

    private String loadPromptFile(String fileName) {
        try {
            Resource resource = resourceLoader.getResource("classpath:prompts/" + fileName + ".txt");
            if (!resource.exists()) resource = resourceLoader.getResource("classpath:prompts/NORMAL.txt");
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            // 리뷰 반영: 예외 발생 시 로그를 남겨 근본 원인 파악이 가능하도록 수정
            log.error("Failed to load prompt file: {}", fileName, e);
            return "전문 면접관으로서 지원자에게 질문을 던져주세요.";
        }
    }
}