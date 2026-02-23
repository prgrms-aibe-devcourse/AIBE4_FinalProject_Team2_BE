package com.aibe.team2.domain.interview.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@Service
public class OpenAiService {

    private final WebClient webClient;

    @Value("${openai.api.key}")
    private String apiKey;

    public OpenAiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.openai.com/v1").build();
    }

    public Flux<String> streamQuestion(String userMessage) {
        return webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(Map.of(
                        "model", "gpt-4o", // 또는 gpt-3.5-turbo
                        "messages", List.of(
                                Map.of("role", "system", "content", "당신은 면접관입니다. 지원자의 답변을 듣고 꼬리 질문을 한 문장으로 하세요."),
                                Map.of("role", "user", "content", userMessage)
                        ),
                        "stream", true // 핵심: 스트리밍 활성화
                ))
                .retrieve()
                .bodyToFlux(String.class); // 응답이 올 때마다 흘려보냄
    }
}