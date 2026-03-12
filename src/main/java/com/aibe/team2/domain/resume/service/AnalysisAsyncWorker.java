package com.aibe.team2.domain.resume.service;

import com.aibe.team2.domain.notification.event.ResumeAnalysisCompleteEvent;
import com.aibe.team2.domain.resume.entity.AnalysisType;
import com.aibe.team2.domain.resume.entity.AnalyzedReport;
import com.aibe.team2.domain.resume.repository.ResumeAnalysisRepository;
import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
public class AnalysisAsyncWorker {

    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final ObjectMapper objectMapper;
    private final WebClient.Builder webClientBuilder;
    private final AnalysisStatusManager statusManager;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent}")
    private String geminiApiUrl;

    @Async("aiAnalysisTaskExecutor")
    @Transactional
    public void processAiAnalysisAsync(Long reportId, String resumeContent) {
        log.info("[Async Worker] AI 분석 시작 - Report ID: {}", reportId);

        AnalyzedReport report = resumeAnalysisRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_404));

        AnalysisType type = report.getAnalysisType();
        String jobDescription = null;
        if (type == AnalysisType.FIT_MATCH && report.getJobPosting() != null) {
            jobDescription = report.getJobPosting().getJobDescription();
        }

        try {
            // 타입에 따라 다른 프롬프트 생성
            String prompt = (type == AnalysisType.NORMAL)
                    ? buildNormalPrompt(resumeContent)
                    : buildMatchPrompt(resumeContent, jobDescription);

            // API 호출
            JsonNode parsedResult = callGeminiApi(prompt);

            // 타입에 따라 다른 메서드로 결과 업데이트
            if (type == AnalysisType.NORMAL) {
                report.completeNormalAnalysis(
                        parsedResult.path("overallFeedback").asText(""),
                        parsedResult.path("sentenceCorrections").toString(),
                        parsedResult.path("revisedFullContent").asText("")
                );
            } else {
                report.completeMatchAnalysis(
                        parsedResult.path("matchingScore").asInt(50),
                        parsedResult.path("matchingFeedback").asText(""),
                        parsedResult.path("keywordAnalysis").toString(),
                        parsedResult.path("overallFeedback").asText(""),
                        parsedResult.path("SentenceCorrections").toString(),
                        parsedResult.path("revisedFullContent").asText("")
                );
            }

            resumeAnalysisRepository.save(report);
            statusManager.changeStatus(reportId, com.aibe.team2.domain.resume.entity.AnalysisStatus.COMPLETED);
            eventPublisher.publishEvent(new ResumeAnalysisCompleteEvent(report.getResume().getMemberId()));

        } catch (WebClientResponseException.TooManyRequests e) {
            log.warn("429 에러 발생. 재시도 초과. Report ID: {}", reportId);
            statusManager.updateToDelayed(reportId);
        } catch (Exception e) {
            log.error("AI 분석 중 오류. Report ID: {}", reportId, e);
            statusManager.updateToFailed(reportId);
        }
    }

    private JsonNode callGeminiApi(String prompt) throws Exception {
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
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(5))
                        .filter(throwable -> throwable instanceof WebClientResponseException.TooManyRequests))
                .timeout(Duration.ofSeconds(45))
                .block();

        String responseText = responseNode.get("candidates").get(0)
                .get("content").get("parts").get(0).get("text").asText();

        return objectMapper.readTree(responseText);
    }

    // 트랙 1: 일반 자소서 첨삭 프롬프트
    private String buildNormalPrompt(String resumeContent) {
        return String.format("""
                당신은 10년 차 전문 에디터입니다. 아래 [자기소개서]의 문맥, 가독성, 표현을 다듬고 첨삭해주세요.
                [자기소개서]
                %s
                
                응답은 반드시 아래 JSON 형식으로 작성하세요.
                {
                  "overallFeedback": "전체적인 글의 흐름은 좋으나, 성과 수치가 부족합니다.",
                  "sentenceCorrections": [
                    { "original": "열심히 했습니다", "corrected": "주도적으로 참여했습니다", "reason": "전문적인 어휘 사용" }
                  ],
                  "revisedFullContent": "전체 교정 완료된 텍스트..."
                }
                """, resumeContent);
    }

    // 트랙 2: 채용공고 기반 매칭 프롬프트
    private String buildMatchPrompt(String resumeContent, String jobDescription) {
        return String.format("""
                당신은 10년 차 수석 채용 면접관입니다. [채용 공고]와 [자기소개서]를 비교하여 적합도를 평가하고 첨삭해주세요.
                [채용 공고]
                %s
                [자기소개서]
                %s
                
                응답은 반드시 아래 JSON 형식으로 작성하세요.
                {
                  "matchingScore": 85,
                  "matchingFeedback": "직무 경험은 우수하나, 클라우드 역량 어필이 부족합니다.",
                  "keywordAnalysis": {
                     "matchedKeywords": ["Java", "Spring"],
                     "missingKeywords": ["AWS"]
                  },
                  "overallFeedback": "전반적인 문맥은 좋으나 공고 맞춤형 수정이 필요합니다.",
                  "corrections": [
                     { "original": "프로젝트를 진행했습니다.", "corrected": "AWS를 활용해 프로젝트를 배포했습니다.", "reason": "공고 요구사항 반영" }
                  ],
                  "revisedFullContent": "공고 맞춤형으로 전체 교정된 텍스트..."
                }
                """, jobDescription, resumeContent);
    }
}