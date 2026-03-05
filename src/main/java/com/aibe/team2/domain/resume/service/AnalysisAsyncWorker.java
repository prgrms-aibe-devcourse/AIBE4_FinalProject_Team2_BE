package com.aibe.team2.domain.resume.service;

import com.aibe.team2.domain.mypage.entity.Member;
import com.aibe.team2.domain.notification.event.ResumeAnalysisCompleteEvent;
import com.aibe.team2.domain.resume.entity.ResumeAnalysisReport;
import com.aibe.team2.domain.resume.repository.ResumeAnalysisRepository;
import com.aibe.team2.domain.notification.service.NotificationService;
import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.BusinessException;
import com.fasterxml.jackson.core.type.TypeReference;
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
import org.springframework.web.reactive.function.client.WebClientRequestException;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisAsyncWorker {

    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final SimilarityEngine similarityEngine;
    private final ObjectMapper objectMapper;
    private final WebClient.Builder webClientBuilder;
    private final AnalysisStatusManager statusManager;
    // [추가] 알림 연동
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationService notificationService;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-3.0-flash:generateContent}")
    private String geminiApiUrl;

    @Async
    @Transactional
    public void processAiAnalysisAsync(Long reportId, String resumeContent, String jobDescription) {
        log.info("[Async Worker] AI 분석 시작 - Report ID: {}", reportId);

        ResumeAnalysisReport report = resumeAnalysisRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_404));

        try {
            // 1. 객관적 지표: 임베딩 기반 코사인 유사도 계산 (0~100점)
            int matchScore = similarityEngine.calculateCosineSimilarityScore(resumeContent, jobDescription);

            // 2. 정성적 지표: Gemini API 호출 (첨삭, 키워드, 소제목 추출)
            AiAnalysisResult result = callGeminiApiForCorrections(resumeContent, jobDescription);

            // 3. 분석 성공 처리 및 DB 업데이트
            report.completeAnalysis(
                    matchScore,
                    result.generatedSubtitle(),
                    result.keywords(),
                    result.corrections(),
                    result.revisedFullContent()
            );
            log.info("[Async Worker] 분석 완료 및 저장 성공 (Score: {}) - Report ID: {}", matchScore, reportId);

            // [추가] 알림 연동
            Long memberId = report.getResume().getMemberId();
            eventPublisher.publishEvent(new ResumeAnalysisCompleteEvent(memberId));
        } catch (WebClientRequestException | TimeoutException e) {
            // 지연 // AI 서버 응답 지연 또는 타임아웃 발생 시 -> DELAYED 상태로 변경
            log.warn("[Async Worker] AI API 호출 지연/타임아웃 발생 - Report ID: {}", reportId, e);
            statusManager.updateToDelayed(reportId);

            // [추가] 알림 연동
            Long memberId = report.getResume().getMemberId();
            Member dummyMember = Member.builder().memberId(memberId).build();
            notificationService.send(dummyMember, "AI_ANALYSIS_DELAYED", "AI 서버 응답이 지연되어 분석이 늦어지고 있습니다. 잠시 후 다시 확인해 주세요.");

        } catch (Exception e) {
            // 실패 // 그 외 파싱 에러나 알 수 없는 에러 발생 시 -> FAILED 상태로 변경
            log.error("[Async Worker] AI 분석 중 오류 발생 - Report ID: {}", reportId, e);
            statusManager.updateToFailed(reportId);

            // [추가] 알림 연동
            Long memberId = report.getResume().getMemberId();
            Member dummyMember = Member.builder().memberId(memberId).build();
            notificationService.send(dummyMember, "AI_ANALYSIS_FAILED", "죄송합니다. 이력서 분석 중 오류가 발생했습니다. 다시 시도해 주세요.");
        }
    }

    private AiAnalysisResult callGeminiApiForCorrections(String resumeContent, String jobDescription) throws Exception {
        String prompt = String.format("""
                당신은 10년 차 전문 채용 담당자이자 자기소개서 첨삭 전문가입니다.
                아래의 [채용 공고]를 참고하여, 지원자의 [자기소개서]가 공고에 적합해 보이도록 첨삭해주세요.
                
                [채용 공고]
                %s
                
                [자기소개서]
                %s
                
                응답은 반드시 아래의 JSON 형식으로만 작성해야 하며, 마크다운이나 부가 설명은 절대 포함하지 마세요.
                {
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
                  "revisedFullContent": "전체 첨삭이 완료된 자기소개서 본문 텍스트"
                }
                """,
                jobDescription != null ? jobDescription : "채용 공고 내용 없음",
                resumeContent != null ? resumeContent : "자기소개서 내용 없음"
        );

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of("responseMimeType", "application/json")
        );

        WebClient webClient = webClientBuilder.baseUrl(geminiApiUrl).build();

        JsonNode responseNode = webClient.post()
                .uri("")
                .header("x-goog-api-key", geminiApiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(30))
                .block();

        if (responseNode == null || !responseNode.has("candidates")) {
            throw new BusinessException(ErrorCode.AI_RESPONSE_PARSE_ERROR);
        }

        String responseText = responseNode.get("candidates").get(0)
                .get("content").get("parts").get(0).get("text").asText();

        JsonNode parsedResult = objectMapper.readTree(responseText);

        return new AiAnalysisResult(
                objectMapper.convertValue(parsedResult.get("generatedSubtitle"), new TypeReference<>() {}),
                objectMapper.convertValue(parsedResult.get("keywords"), new TypeReference<>() {}),
                objectMapper.convertValue(parsedResult.get("corrections"), new TypeReference<>() {}),
                parsedResult.get("revisedFullContent").asText()
        );
    }

    private record AiAnalysisResult(
            Map<String, Object> generatedSubtitle,
            Map<String, Object> keywords,
            Map<String, Object> corrections,
            String revisedFullContent
    ) {}
}