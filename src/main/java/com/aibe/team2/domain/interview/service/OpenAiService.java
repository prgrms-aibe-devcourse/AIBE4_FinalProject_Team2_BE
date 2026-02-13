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

    @Value("${openai.api.key}") // application.yml의 환경변수 참조
    private String apiKey;

    public OpenAiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.openai.com/v1").build();
    }

    public Flux<String> streamQuestion(String userMessage) {
        return webClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .bodyValue(Map.of(
                        "model", "gpt-4o",
                        "messages", List.of(
                                Map.of("role", "system", "content", "당신은 전문 면접관입니다. 자소서 내용을 바탕으로 날카로운 꼬리 질문을 한 문장으로 하세요."),
                                Map.of("role", "user", "content", userMessage)
                        ),
                        "stream", true
                ))
                .retrieve()
                .bodyToFlux(String.class);
    }
}