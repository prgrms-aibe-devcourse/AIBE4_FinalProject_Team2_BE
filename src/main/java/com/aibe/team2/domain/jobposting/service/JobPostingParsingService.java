package com.aibe.team2.domain.jobposting.service;

import com.aibe.team2.domain.jobposting.dto.JobPostingParseResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobPostingParsingService {
    //
    private final ObjectMapper objectMapper;
    private final JobPostingCrawlerService jobPostingCrawlerService;
    private final RestTemplate restTemplate;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    public JobPostingParseResponse parseFromUrl(String url) {
        // 1. Jsoup 크롤링으로 기본 데이터 가져오기
        Map<String, String> crawledData = jobPostingCrawlerService.crawlFullText(url);

        if (crawledData.isEmpty() || crawledData.get("fullDescription") == null) {
            throw new IllegalArgumentException("크롤링할 수 없는 URL이거나 내용이 비어있습니다.");
        }

        String companyName = extractCompanyName(crawledData.get("title"));
        String jobTitle = crawledData.get("title");
        String mainTasks = crawledData.getOrDefault("mainTasks", "상세 내용 참고");
        String qualifications = crawledData.getOrDefault("qualifications", "상세 내용 참고");

        // DB 저장을 위해 긴 본문은 500자로 커트
        String jobDesc = crawledData.get("fullDescription");
        if (jobDesc != null && jobDesc.length() > 500) {
            jobDesc = jobDesc.substring(0, 500) + "... (이하 생략)";
        }

        // 기본값 세팅 (없으면 "없음")
        String preferred = crawledData.getOrDefault("preferred", "없음");
        String benefits = crawledData.getOrDefault("benefits", "없음");
        List<String> requiredSkills = Collections.emptyList();
        List<String> expectedQuestions = Collections.emptyList();

        // 2. AI를 통해 누락된 정보(우대, 복지, 스킬)와 면접 질문 5개 동시 추출
        try {
            String prompt = buildParsingPrompt(crawledData);
            String aiJsonResponse = callGeminiApi(prompt);

            if (!"{}".equals(aiJsonResponse) && !aiJsonResponse.isBlank() && !"[]".equals(aiJsonResponse)) {
                aiJsonResponse = aiJsonResponse.replace("```json", "").replace("```", "").trim();

                // JsonNode로 파싱하여 안전하게 데이터 추출
                JsonNode rootNode = objectMapper.readTree(aiJsonResponse);

                if (rootNode.has("preferred") && !rootNode.get("preferred").isNull()) {
                    preferred = rootNode.get("preferred").asText();
                }
                if (rootNode.has("benefits") && !rootNode.get("benefits").isNull()) {
                    benefits = rootNode.get("benefits").asText();
                }
                if (rootNode.has("requiredSkills") && rootNode.get("requiredSkills").isArray()) {
                    requiredSkills = objectMapper.convertValue(rootNode.get("requiredSkills"), new TypeReference<List<String>>() {});
                }
                if (rootNode.has("expectedQuestions") && rootNode.get("expectedQuestions").isArray()) {
                    expectedQuestions = objectMapper.convertValue(rootNode.get("expectedQuestions"), new TypeReference<List<String>>() {});
                }
            }
        } catch (Exception e) {
            log.warn("⚠️ AI 정보 추출 실패. 크롤링 기본값으로 저장합니다. 원인: {}", e.getMessage());
        }

        // 3. 최종 조합하여 반환
        return new JobPostingParseResponse(
                companyName,
                jobTitle,
                jobDesc,
                mainTasks,
                qualifications,
                preferred,
                benefits,
                requiredSkills,
                expectedQuestions
        );
    }

    private String callGeminiApi(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of("responseMimeType", "application/json")
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String requestUrl = geminiApiUrl + "?key=" + geminiApiKey;
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            try {
                ResponseEntity<JsonNode> response = restTemplate.postForEntity(requestUrl, requestEntity, JsonNode.class);
                JsonNode responseNode = response.getBody();

                if (responseNode != null && responseNode.has("candidates") && responseNode.get("candidates").isArray() && !responseNode.get("candidates").isEmpty()) {
                    JsonNode candidate = responseNode.get("candidates").get(0);
                    if (candidate.has("content") && candidate.get("content").has("parts") && candidate.get("content").get("parts").isArray() && !candidate.get("content").get("parts").isEmpty()) {
                        return candidate.get("content").get("parts").get(0).get("text").asText();
                    }
                }
                return "{}";

            } catch (HttpClientErrorException.TooManyRequests e) {
                long waitTime = 5000L * (i + 1);
                log.warn("⚠️ [429 Error] API 속도 제한. {}초 대기 후 재시도... ({}/{})", waitTime / 1000, i + 1, maxRetries);
                try { Thread.sleep(waitTime); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
            } catch (Exception e) {
                log.error("⚠️ Gemini API RestTemplate 통신 오류: {}", e.getMessage());
                break;
            }
        }
        return "{}";
    }

    private String buildParsingPrompt(Map<String, String> crawledData) {
        String coreInfo = "주요업무: " + crawledData.getOrDefault("mainTasks", "")
                + "\n자격요건: " + crawledData.getOrDefault("qualifications", "")
                + "\n전체내용: " + crawledData.getOrDefault("fullDescription", "");

        // 토큰 초과 방지를 위해 1500자로 커트
        if (coreInfo.length() > 1500) {
            coreInfo = coreInfo.substring(0, 1500);
        }

        return String.format("""
            아래 채용 공고 내용을 분석하여 누락된 정보를 채우고 면접 질문을 생성해.
            반드시 아래의 JSON 포맷으로만 응답해.
            
            {
              "preferred": "우대사항을 찾아 3줄로 요약 (없으면 '없음')",
              "benefits": "복지 또는 혜택을 찾아 3줄로 요약 (없으면 '없음')",
              "requiredSkills": ["Java", "Spring Boot", "React", "등 주요 요구 기술 스택 배열"],
              "expectedQuestions": ["직무 역량 검증을 위한 심층 면접 질문 1", "질문 2", "질문 3", "질문 4", "질문 5"]
            }
            
            [공고 내용]:
            %s
            """, coreInfo);
    }

    private String extractCompanyName(String title) {
        if (title == null || title.isBlank()) return "기업명 미상";
        String cleanedTitle = title.replaceAll("\\[.*?\\]|\\(.*?\\)", "").trim();
        return cleanedTitle.isEmpty() ? "기업명 미상" : cleanedTitle.split(" ")[0];
    }
}