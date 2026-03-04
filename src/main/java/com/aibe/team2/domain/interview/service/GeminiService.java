package com.aibe.team2.domain.interview.service;

import com.aibe.team2.domain.interview.dto.InterviewRequestDto;
import com.aibe.team2.domain.interview.enums.InterviewMode;
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
import java.util.Arrays;
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

        String atmospherePrompt = loadPromptFile(request.getInterviewMode().name());
        String constraints = loadPromptFile("constraints");
        String finalPrompt = String.format("%s\n\n%s\n\n[Candidate Answer]\n%s",
                atmospherePrompt, constraints, request.getMessage());

        log.info("[Gemini-Success] Session: {}, Atmosphere: {}", sessionId, request.getInterviewMode());

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
        boolean isAllowed = Arrays.stream(InterviewMode.values())
                .anyMatch(mode -> mode.name().equals(fileName)) || "constraints".equals(fileName);

        String targetFileName = fileName;

        if (!isAllowed) {
            log.error("보안 위협 감지: 허용되지 않은 프롬프트 파일 접근 시도 - {}", fileName);
            // 비정상적인 접근일 경우 안전한 기본값(NORMAL)으로 강제 전환
            targetFileName = "NORMAL";
        }

        try {
            Resource resource = resourceLoader.getResource("classpath:prompts/" + targetFileName + ".txt");
            if (!resource.exists()) {
                log.warn("⚠️ 프롬프트 파일이 존재하지 않아 기본 설정을 로드합니다: {}", targetFileName);
                resource = resourceLoader.getResource("classpath:prompts/NORMAL.txt");
            }
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("❌ 프롬프트 파일 로드 중 오류 발생 [파일명: {}]: ", targetFileName, e);
            return "면접관으로서 질문을 던져주세요.";
        }
    }
}