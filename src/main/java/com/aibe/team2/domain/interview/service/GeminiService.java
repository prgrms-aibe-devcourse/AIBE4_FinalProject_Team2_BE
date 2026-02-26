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

        // 🚀 1. 프롬프트 세분화: 시니어 IT 면접관 페르소나 부여
        String detailedPrompt =
                "[Persona]\n" +
                        "당신은 글로벌 IT 기업의 10년차 시니어 기술 면접관입니다. 지원자의 답변을 분석하여 논리적 허점을 날카롭게 파고듭니다.\n\n" +
                        "[Interview Rules]\n" +
                        "1. 답변의 기술적 키워드를 포착하여 깊이 있는 원리를 묻는 꼬리 질문을 하세요.\n" +
                        "2. 답변이 추상적일 경우 구체적인 상황(Situation)과 수치적 성과를 요구하세요.\n" +
                        "3. 지원자가 당황할 수 있는 예리한 질문을 던지되, 태도는 정중함을 유지하세요.\n\n" +
                        "[Constraints]\n" +
                        "1. 반드시 한국어로만 질문하세요.\n" +
                        "2. 대화의 흐름을 위해 딱 '한 문장'의 질문만 생성하세요.\n" +
                        "3. '아하', '그렇군요' 같은 추임새는 생략하고 바로 질문만 하세요.\n\n" +
                        "[Candidate Answer]\n" +
                        (userMessage == null || userMessage.trim().isEmpty() ? "안녕하세요. 면접을 시작하려 합니다." : userMessage);

        // URL 조립: 주소 중복 방지 및 보안 강화
        // .env에 어떤 형식으로 주소가 들어와도 도메인만 추출
        String rootUrl = baseUrl.split("/v1")[0];
        if (rootUrl.endsWith("/")) {
            rootUrl = rootUrl.substring(0, rootUrl.length() - 1);
        }

        // URL에서 ?key= 부분을 제거 (로그 노출 방지)
        String fullUrl = rootUrl + "/v1beta/models/gemini-flash-latest:streamGenerateContent?alt=sse";

        return webClient.post()
                .uri(URI.create(fullUrl))
                // 🛡️ 보안 처리: API 키를 주소가 아닌 HTTP 헤더에 담아 보냅니다.
                .header("x-goog-api-key", apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(Map.of(
                        "contents", List.of(
                                Map.of("role", "user",
                                        "parts", List.of(Map.of("text", detailedPrompt)))
                        )
                ))
                .retrieve()
                .bodyToFlux(String.class)
                .doOnError(WebClientResponseException.class, e -> {
                    System.err.println("=== 🚨 Gemini API 호출 에러 🚨 ===");
                    System.err.println("상태 코드: " + e.getStatusCode());
                    System.err.println("응답 본문: " + e.getResponseBodyAsString());
                    System.err.println("호출 주소: " + fullUrl);
                });
    }
}