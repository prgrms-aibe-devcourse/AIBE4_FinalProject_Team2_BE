package com.aibe.team2.domain.interview.service;

import com.aibe.team2.domain.interview.dto.GeminiResponseDto;
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

        // JSON 응답을 강제하기 위한 설정
        Map<String, Object> generationConfig = Map.of(
                "responseMimeType", "application/json"
        );

        // System Instruction과 User Content(대화 기록)의 분리 및 태그 적용
        Map<String, Object> requestBody = Map.of(
                "systemInstruction", Map.of(
                        "parts", List.of(Map.of("text", systemPrompt))
                ),
                "contents", List.of(
                        Map.of("role", "user",
                                "parts", List.of(Map.of("text", "<conversation_records>\n" + recordsText + "\n</conversation_records>")))
                ),
                "generationConfig", generationConfig
        );

        String analysisUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

        try {
            String responseJson = webClientBuilder.build()
                    .post()
                    .uri(analysisUrl)
                    .header("x-goog-api-key", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return extractText(responseJson);
        } catch (Exception e) {
            log.error("❌ AI 분석 호출 실패: {}", e.getMessage());
            throw new RuntimeException("분석 API 호출 중 오류 발생");
        }
    }

    private String extractText(String responseJson) throws Exception {
        GeminiResponseDto responseDto = objectMapper.readValue(responseJson, GeminiResponseDto.class);

        if (responseDto.getCandidates() == null || responseDto.getCandidates().isEmpty() ||
                responseDto.getCandidates().get(0).getContent() == null ||
                responseDto.getCandidates().get(0).getContent().getParts() == null ||
                responseDto.getCandidates().get(0).getContent().getParts().isEmpty()) {
            throw new RuntimeException("Gemini API 응답에서 텍스트를 추출할 수 없습니다.");
        }

        String text = responseDto.getCandidates().get(0).getContent().getParts().get(0).getText();
        if (text == null) return "";

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