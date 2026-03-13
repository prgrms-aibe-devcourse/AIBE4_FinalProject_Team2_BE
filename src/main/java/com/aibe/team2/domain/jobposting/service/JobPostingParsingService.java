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
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobPostingParsingService {

    private final ObjectMapper objectMapper;
    private final JobPostingCrawlerService jobPostingCrawlerService;
    private final WebClient.Builder webClientBuilder;

    private WebClient webClient;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent}")
    private String geminiApiUrl;

    @PostConstruct
    public void init() {
        // 객체 생성 비용을 줄이기 위해 빈 초기화 시점에 WebClient 빌드 및 재사용
        this.webClient = webClientBuilder.build();
    }

    public JobPostingParseResponse autoFillFromUrl(String url) {
        String rawText = jobPostingCrawlerService.crawlFullText(url).toString();

        if (rawText == null || rawText.isBlank()) {
            throw new IllegalArgumentException("크롤링할 수 없는 URL이거나 내용이 비어있습니다.");
        }

        String prompt = """
            다음 채용 공고 텍스트를 분석해서 아래 JSON 형식에 맞게 데이터를 추출해줘.
            공고 내용을 바탕으로 지원자에게 물어볼 만한 핵심 예상 면접 질문 5가지도 함께 생성해.
            반드시 순수 JSON 포맷으로만 대답해. (Markdown 텍스트 블록 제외)
            
            {
              "companyName": "기업명",
              "jobTitle": "직무명",
              "jobDescription": "공고 전체 내용 요약 또는 원본",
              "mainTasks": "주요업무 내용",
              "qualifications": "자격요건 내용",
              "preferred": "우대사항 내용",
              "benefits": "복리후생 내용",
              "requiredSkills": ["Java", "Spring Boot", "MySQL" 등 기술 스택 배열],
              "expectedQuestions": [
                 "공고의 주요 업무와 관련된 예상 질문 1",
                 "공고의 자격 요건을 검증하는 예상 질문 2",
                 "예상 질문 3",
                 "예상 질문 4",
                 "예상 질문 5"
              ]
            }
            
            [채용 공고 본문]
            """ + rawText;

        try {
            // 1. 직접 Gemini API 호출
            String aiJsonResponse = callGeminiApi(prompt);

            // 2. 응답받은 순수 JSON 문자열을 DTO 객체로 즉시 매핑
            JobPostingParseResponse response = objectMapper.readValue(aiJsonResponse, JobPostingParseResponse.class);

            // 3. 기업명 노이즈(괄호 등) 정제 후 최종 DTO 반환
            String refinedCompanyName = extractCompanyName(response.companyName());

            return new JobPostingParseResponse(
                    refinedCompanyName,
                    response.jobTitle(),
                    response.jobDescription(),
                    response.mainTasks(),
                    response.qualifications(),
                    response.preferred(),
                    response.benefits(),
                    response.requiredSkills(),
                    response.expectedQuestions()
            );

        } catch (Exception e) {
            log.error("AI 채용공고 분석 및 파싱 실패. URL: {}", url, e);
            // 실패 시 빈 데이터를 반환하여 프론트엔드 에러 방지
            return new JobPostingParseResponse(null, null, null, null, null, null, null, null, java.util.Collections.emptyList());
        }
    }

    /**
     * Gemini API를 HTTP 기반으로 직접 호출하는 로직
     */
    private String callGeminiApi(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of("responseMimeType", "application/json")
        );

        JsonNode responseNode = webClient.post()
                .uri(URI.create(geminiApiUrl))
                .header("x-goog-api-key", geminiApiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(JsonNode.class)
                // 429 에러(Too Many Requests) 발생 시 3초 간격으로 최대 3번 재시도
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(3))
                        .filter(throwable -> throwable instanceof WebClientResponseException.TooManyRequests))
                .timeout(Duration.ofSeconds(30)) // 30초 타임아웃
                .block();

        if (responseNode != null && responseNode.has("candidates")) {
            return responseNode.get("candidates").get(0)
                    .get("content").get("parts").get(0).get("text").asText();
        }

        return "{}";
    }

    /**
     * 괄호 등 노이즈가 포함된 기업명 데이터를 깔끔하게 추출하는 로직
     */
    private String extractCompanyName(String title) {
        if (title == null || title.isBlank()) return "기업명 미상";

        // 대괄호 [], 소괄호 () 및 그 안의 문자열 일괄 제거
        String cleanedTitle = title.replaceAll("\\[.*?]|\\(.*?\\)", "").trim();

        // 괄호 등을 제거한 후 문자열이 비어있다면, 유효한 회사명이 없는 것으로 간주합니다.
        // 예: "(주)", "[신입]"
        if (cleanedTitle.isEmpty()) {
            return "기업명 미상";
        }

        // 첫 번째 단어를 회사명으로 간주합니다. (예: "삼성전자 주식회사" -> "삼성전자")
        return cleanedTitle.split(" ")[0];
    }
}