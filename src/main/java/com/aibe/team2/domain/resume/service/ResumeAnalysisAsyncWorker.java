package com.aibe.team2.domain.resume.service;

import com.aibe.team2.domain.resume.entity.ResumeAnalysisReport;
import com.aibe.team2.domain.resume.repository.ResumeAnalysisRepository;
import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.BusinessException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeAnalysisAsyncWorker {

    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final ObjectMapper objectMapper;
    private final WebClient.Builder webClientBuilder;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-3.0-flash:generateContent}")
    private String geminiApiUrl;

    /**
     * 별도의 백그라운드 스레드에서 실행되는 AI 분석 로직
     */
    @Async // 이 메서드는 호출 즉시 리턴되고, 내부 로직은 백그라운드에서 돕니다.
    @Transactional
    public void processAiAnalysisAsync(Long reportId, String resumeContent, String jobDescription) {
        log.info("[Async Worker] AI 분석 시작 - Report ID: {}", reportId);

        // 1. 트랜잭션이 분리되었으므로 DB에서 리포트를 다시 조회해옵니다.
        ResumeAnalysisReport report = resumeAnalysisRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_404));

        try {
            // 2. Gemini API 호출
            AiAnalysisResult result = callGeminiApi(resumeContent, jobDescription);

            // 3. 분석 성공 처리 및 DB 업데이트
            report.completeAnalysis(
                    result.score(),
                    result.generatedSubtitle(),
                    result.keywords(),
                    result.corrections(),
                    result.revisedFullContent()
            );
            log.info("[Async Worker] AI 분석 완료 및 저장 성공 - Report ID: {}", reportId);

        } catch (Exception e) {
            log.error("[Async Worker] AI 분석 중 오류 발생 - Report ID: {}", reportId, e);
            report.failAnalysis();
        }
    }

    private AiAnalysisResult callGeminiApi(String resumeContent, String jobDescription) throws Exception {
        String prompt = String.format("""
                당신은 10년 차 전문 채용 담당자이자 이력서 첨삭 전문가입니다.
                아래의 [채용 공고]와 지원자의 [이력서]를 분석하고, 합격률을 높일 수 있도록 이력서를 첨삭해주세요.
                
                [채용 공고]
                %s
                
                [이력서]
                %s
                
                응답은 반드시 아래의 JSON 형식으로만 작성해야 하며, 마크다운이나 부가 설명은 절대 포함하지 마세요.
                {
                  "score": 85,
                  "generatedSubtitle": {
                    "title": "데이터 분석 역량을 갖춘 백엔드 개발자",
                    "reason": "이 소제목을 추천하는 이유"
                  },
                  "keywords": {
                    "goodKeywords": ["분석력", "꾸준함"],
                    "missingKeywords": ["대규모 트래픽", "시스템 설계"]
                  },
                  "corrections": {
                    "corrections": [
                      {
                        "originalSentence": "기존 문장",
                        "correctedSentence": "교정된 문장",
                        "reason": "구체적인 성과 위주로 서술하는 것이 좋습니다."
                      }
                    ]
                  },
                  "revisedFullContent": "전체 첨삭이 완료된 이력서의 전체 본문 텍스트"
                }
                """,
                jobDescription != null ? jobDescription : "채용 공고 내용 없음",
                resumeContent != null ? resumeContent : "이력서 내용 없음"
        );

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of("responseMimeType", "application/json")
        );

        WebClient webClient = webClientBuilder.baseUrl(geminiApiUrl).build();

        // 비동기 스레드 내부이므로 block()을 써도 메인 스레드에 영향을 주지 않아 안전합니다.
        JsonNode responseNode = webClient.post()
                .uri("")
                .header("x-goog-api-key", geminiApiKey) // Security 경고 해결!
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new BusinessException(ErrorCode.AI_INVALID_REQUEST)))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new BusinessException(ErrorCode.AI_SERVER_ERROR)))
                .bodyToMono(JsonNode.class)
                .block();

        if (responseNode == null || !responseNode.has("candidates")) {
            throw new BusinessException(ErrorCode.AI_RESPONSE_PARSE_ERROR);
        }

        String responseText = responseNode.get("candidates").get(0)
                .get("content").get("parts").get(0).get("text").asText();

        JsonNode parsedResult = objectMapper.readTree(responseText);

        return new AiAnalysisResult(
                parsedResult.get("score").asInt(),
                objectMapper.convertValue(parsedResult.get("generatedSubtitle"), new TypeReference<>() {}),
                objectMapper.convertValue(parsedResult.get("keywords"), new TypeReference<>() {}),
                objectMapper.convertValue(parsedResult.get("corrections"), new TypeReference<>() {}),
                parsedResult.get("revisedFullContent").asText()
        );
    }

    private record AiAnalysisResult(
            Integer score,
            Map<String, Object> generatedSubtitle,
            Map<String, Object> keywords,
            Map<String, Object> corrections,
            String revisedFullContent
    ) {}
}