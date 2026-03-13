package com.aibe.team2.domain.jobposting.service;

import com.aibe.team2.domain.jobposting.dto.JobPostingParseResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobPostingParsingService {

    private final JobPostingCrawlerService crawlerService;
    private final ObjectMapper objectMapper;
    private final WebClient.Builder webClientBuilder; // API 통신을 위한 WebClient 주입

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent}")
    private String geminiApiUrl;

    /**
     * URL 크롤링 후 직접 Gemini API를 호출하여 예상 질문 및 역량 파싱
     */
    public JobPostingParseResponse autoFillFromUrl(String url) {

        // 1. 크롤링 전담 서비스 호출
        Map<String, String> crawledData = crawlerService.crawlAndExtract(url);

        if (crawledData.isEmpty() || crawledData.get("fullDescription") == null) {
            throw new IllegalArgumentException("크롤링할 수 없는 URL이거나 내용이 비어있습니다.");
        }

        // 2. AI를 통한 예상 질문 및 스킬 도출
        String prompt = buildPromptForQuestions(crawledData);
        List<String> expectedQuestions = new ArrayList<>();
        List<String> requiredSkills = new ArrayList<>();

        try {
            // GeminiService 대신 내부 메서드로 API 직접 호출
            String aiJsonResponse = callGeminiApi(prompt);
            JsonNode parsedNode = objectMapper.readTree(aiJsonResponse);

            parsedNode.path("expectedQuestions").forEach(q -> expectedQuestions.add(q.asText()));
            parsedNode.path("requiredSkills").forEach(s -> requiredSkills.add(s.asText()));
        } catch (Exception e) {
            log.error("AI 예상 질문 파싱 실패", e);
        }

        // 3. 크롤링 데이터 + AI 생성 데이터 병합 후 반환
        return new JobPostingParseResponse(
                extractCompanyName(crawledData.get("title")),
                crawledData.get("title"),
                crawledData.get("fullDescription"),
                crawledData.get("mainTasks"),
                crawledData.get("qualifications"),
                crawledData.get("preferred"),
                crawledData.get("benefits"),
                requiredSkills,
                expectedQuestions // 생성된 예상 질문 5가지!
        );
    }

    /**
     * Gemini API 직접 호출 및 JSON 추출 로직
     */
    private String callGeminiApi(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of("responseMimeType", "application/json")
        );

        WebClient webClient = webClientBuilder.build();
        JsonNode responseNode = webClient.post()
                .uri(URI.create(geminiApiUrl))
                .header("x-goog-api-key", geminiApiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(JsonNode.class)
                // 429(Too Many Requests) 에러 발생 시 최대 3번까지 재시도
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(3))
                        .filter(throwable -> throwable instanceof WebClientResponseException.TooManyRequests))
                .timeout(Duration.ofSeconds(30))
                .block();

        if (responseNode != null && responseNode.has("candidates")) {
            return responseNode.get("candidates").get(0)
                    .get("content").get("parts").get(0).get("text").asText();
        }

        return "{}";
    }

    private String buildPromptForQuestions(Map<String, String> crawledData) {
        return String.format("""
            아래 채용 공고 정보를 분석하여, 지원자에게 물어볼 만한 핵심 예상 면접 질문 5가지와 요구 스택을 추출해줘.
            반드시 순수 JSON 포맷으로만 대답해. (마크다운 불필요)
            
            {
              "requiredSkills": ["Java", "Spring Boot", "MySQL"],
              "expectedQuestions": ["질문 1", "질문 2", "질문 3", "질문 4", "질문 5"]
            }
            
            [공고 주요업무]: %s
            [공고 자격요건]: %s
            [공고 우대사항]: %s
            [공고 전체본문]: %s
            """,
                crawledData.getOrDefault("mainTasks", "없음"),
                crawledData.getOrDefault("qualifications", "없음"),
                crawledData.getOrDefault("preferred", "없음"),
                crawledData.get("fullDescription")
        );
    }

    private String extractCompanyName(String title) {
        if (title == null) return "기업명 미상";
        return title.split(" ")[0]; // 간단한 타이틀 자르기 로직
    }
}