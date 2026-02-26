package com.aibe.team2.domain.interview.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    private final WebClient webClient;
    private final String apiKey;
    private final String baseUrl;

    public GeminiService(WebClient.Builder webClientBuilder,
                         @Value("${gemini.api.key}") String apiKey,
                         @Value("${gemini.api.url}") String baseUrl) {
        this.apiKey = apiKey.trim();
        this.baseUrl = baseUrl.trim();
        this.webClient = webClientBuilder.build();
    }

    public Flux<String> streamQuestion(String userMessage) {

        // 🚀 지시사항과 답변을 합쳐서 보냅니다.
        String prompt = "명령: 당신은 전문 면접관입니다. 지원자의 답변을 듣고 예리한 꼬리 질문을 한 문장으로 하세요.\n\n지원자 답변: " +
                (userMessage == null || userMessage.trim().isEmpty() ? "안녕하세요." : userMessage);

        // 🚀 [핵심 수정] 목록에서 확인된 실제 모델명 'gemini-2.0-flash'를 사용합니다!
        String cleanUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String fullUrl = cleanUrl + "/v1beta/models/gemini-flash-latest:streamGenerateContent?key=" + apiKey + "&alt=sse";

        System.out.println("=== 🚀 Gemini 2.0 Flash 모델로 연결 시도 ===");

        return webClient.post()
                .uri(URI.create(fullUrl))
                .header("Content-Type", "application/json")
                .bodyValue(Map.of(
                        "contents", List.of(
                                Map.of("role", "user", "parts", List.of(Map.of("text", prompt)))
                        )
                ))
                .retrieve()
                .bodyToFlux(String.class)
                .doOnError(WebClientResponseException.class, e -> {
                    System.err.println("=== 🚨 에러 발생 상세 내용 🚨 ===");
                    System.err.println("상태 코드: " + e.getStatusCode());
                    System.err.println("응답 본문: " + e.getResponseBodyAsString());
                    System.err.println("================================");
                });
    }
}