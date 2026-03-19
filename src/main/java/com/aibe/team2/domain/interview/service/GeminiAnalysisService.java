package com.aibe.team2.domain.interview.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiAnalysisService {

    private final WebClient.Builder webClientBuilder;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String apiKey;

    public String analyzeInterviewSync(String recordsText) {
        String systemPrompt = loadPromptFile("ANALYSIS");
        String fullPrompt = systemPrompt + "\n\n" + recordsText;

        // [추가] JSON 응답을 강제하기 위한 설정 (파싱 안정성 극대화)
        Map<String, Object> generationConfig = Map.of(
                "responseMimeType", "application/json"
        );

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", fullPrompt)))
                ),
                "generationConfig", generationConfig // 설정 주입
        );

        // 최신 모델인 gemini-2.5-flash로 엔드포인트 URL 적용 완료
        String analysisUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;

        try {
            String responseJson = webClientBuilder.build()
                    .post()
                    .uri(analysisUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // 전체 JSON 응답이 올 때까지 대기 (Sync)

            return extractText(responseJson);
        } catch (Exception e) {
            log.error("❌ AI 분석 호출 실패: {}", e.getMessage());
            throw new RuntimeException("분석 API 호출 중 오류 발생");
        }
    }

    private String extractText(String responseJson) throws Exception {
        Map<String, Object> map = objectMapper.readValue(responseJson, Map.class);
        List<Map<String, Object>> candidates = (List<Map<String, Object>>) map.get("candidates");
        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
        String text = (String) parts.get(0).get("text");

        // Gemini가 가끔 마크다운 ```json ... ``` 으로 감싸서 보내는 것을 정리
        return text.replaceAll("(?s)^```json\\s*", "").replaceAll("(?s)\\s*```$", "").trim();
    }

    private String loadPromptFile(String fileName) {
        try {
            Resource resource = resourceLoader.getResource("classpath:prompts/" + fileName + ".txt");
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("프롬프트 파일 로드 실패: {}", fileName, e);
            return "면접 기록을 분석하여 JSON으로 응답하세요.";
        }
    }
}