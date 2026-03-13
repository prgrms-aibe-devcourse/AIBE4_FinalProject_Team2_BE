package com.aibe.team2.domain.jobposting.service;

import com.aibe.team2.domain.jobposting.dto.JobPostingParseResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
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

    private final WebClient.Builder webClientBuilder;
    // [리뷰 반영] WebClient 인스턴스를 필드로 선언하여 재사용
    private WebClient webClient;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent}")
    private String geminiApiUrl;

    @PostConstruct
    public void init() {
        // [리뷰 반영] 빈 초기화 시점에 한 번만 빌드하여 불필요한 객체 생성 방지
        this.webClient = webClientBuilder.build();
    }

    public JobPostingParseResponse autoFillFromUrl(String url) {
        Map<String, String> crawledData = crawlerService.crawlAndExtract(url);

        if (crawledData.isEmpty() || crawledData.get("fullDescription") == null) {
            throw new IllegalArgumentException("크롤링할 수 없는 URL이거나 내용이 비어있습니다.");
        }

        String prompt = buildPromptForQuestions(crawledData);
        List<String> expectedQuestions = new ArrayList<>();
        List<String> requiredSkills = new ArrayList<>();

        try {
            String aiJsonResponse = callGeminiApi(prompt);
            JsonNode parsedNode = objectMapper.readTree(aiJsonResponse);

            parsedNode.path("expectedQuestions").forEach(q -> expectedQuestions.add(q.asText()));
            parsedNode.path("requiredSkills").forEach(s -> requiredSkills.add(s.asText()));
        } catch (Exception e) {
            log.error("AI 예상 질문 파싱 실패", e);
        }

        return new JobPostingParseResponse(
                extractCompanyName(crawledData.get("title")),
                crawledData.get("title"),
                crawledData.get("fullDescription"),
                crawledData.get("mainTasks"),
                crawledData.get("qualifications"),
                crawledData.get("preferred"),
                crawledData.get("benefits"),
                requiredSkills,
                expectedQuestions
        );
    }

    private String callGeminiApi(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of("responseMimeType", "application/json")
        );

        // 메서드 내에서 매번 build() 하던 로직 제거, 재사용된 필드 사용
        JsonNode responseNode = webClient.post()
                .uri(URI.create(geminiApiUrl))
                .header("x-goog-api-key", geminiApiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(JsonNode.class)
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

    // [리뷰 반영] 정규식을 활용하여 견고해진 회사명 파싱 로직
    private String extractCompanyName(String title) {
        if (title == null || title.isBlank()) return "기업명 미상";

        // 1. 대괄호 [], 소괄호 () 및 그 안의 문자열(예: [신입/경력], (주) 등)을 일괄 제거
        String cleanedTitle = title.replaceAll("\\[.*?\\]|\\(.*?\\)", "").trim();

        // 2. 만약 괄호를 제거했더니 문자열이 비어버린 경우 방어 로직
        if (cleanedTitle.isEmpty()) {
            return title.split(" ")[0];
        }

        // 3. 정제된 문자열의 가장 첫 단어를 반환 (예: "카카오 백엔드..." -> "카카오")
        return cleanedTitle.split(" ")[0];
    }
}