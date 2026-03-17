package com.aibe.team2.domain.resume.service;

import com.aibe.team2.domain.admin.service.QueueJobMetricService;
import com.aibe.team2.domain.jobposting.entity.JobPosting;
import com.aibe.team2.domain.jobposting.entity.JobSkill;
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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisAsyncWorker {

    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final ObjectMapper objectMapper;
    private final WebClient.Builder webClientBuilder;
    private final AnalysisStatusManager statusManager;
    private final ApplicationEventPublisher eventPublisher;
    private final QueueJobMetricService queueJobMetricService;

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent}")
    private String geminiApiUrl;

    @Async("aiAnalysisTaskExecutor")
    @Transactional
    public void processAiAnalysisAsync(Long reportId, String resumeContent,Long queueJobMetricId ) {
        log.info("[Async Worker] AI 분석 시작 - Report ID: {}", reportId);

        AnalyzedReport report = resumeAnalysisRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_404));

        AnalysisType type = report.getAnalysisType();
        String jobInfoText = "";

        // 매칭 분석일 경우 구조화된 공고 데이터를 프롬프트용 텍스트로 조립
        if (type == AnalysisType.FIT_MATCH && report.getJobPosting() != null) {
            JobPosting jp = report.getJobPosting();

            // 요구 역량(Skill) 리스트 추출
            String skills = jp.getJobSkills().stream()
                    .map(JobSkill::getSkillName)
                    .collect(Collectors.joining(", "));

            jobInfoText = String.format("""
                [채용 공고 상세 정보]
                - 전체 설명: %s
                - 주요 업무: %s
                - 자격 요건: %s
                - 우대 사항: %s
                - 복리 후생: %s
                - 요구 역량: %s
                """,
                    jp.getJobDescription() != null ? jp.getJobDescription() : "없음",
                    jp.getMainTasks() != null ? jp.getMainTasks() : "없음",
                    jp.getQualifications() != null ? jp.getQualifications() : "없음",
                    jp.getPreferred() != null ? jp.getPreferred() : "없음",
                    jp.getBenefits() != null ? jp.getBenefits() : "없음",
                    !skills.isEmpty() ? skills : "없음"
            );
        }

        try {
            String prompt = (type == AnalysisType.NORMAL)
                    ? buildNormalPrompt(resumeContent)
                    : buildMatchPrompt(resumeContent, jobInfoText);

            JsonNode parsedResult = callGeminiApi(prompt);

            if (type == AnalysisType.NORMAL) {
                report.completeNormalAnalysis(
                        parsedResult.path("overallFeedback").asText(""),
                        parsedResult.path("sentenceCorrections").toString(),
                        parsedResult.path("paragraphSummaries").toString(),
                        parsedResult.path("revisedFullContent").asText("")
                );
            } else {
                report.completeMatchAnalysis(
                        parsedResult.path("matchingScore").asInt(50),
                        parsedResult.path("matchingFeedback").asText(""),
                        parsedResult.path("keywordAnalysis").toString(),
                        parsedResult.path("expectedQuestions").toString(),
                        parsedResult.path("overallFeedback").asText(""),
                        parsedResult.path("corrections").toString(),
                        parsedResult.path("paragraphSummaries").toString(),
                        parsedResult.path("revisedFullContent").asText("")
                );
            }

            resumeAnalysisRepository.save(report);
            statusManager.changeStatus(reportId, com.aibe.team2.domain.resume.entity.AnalysisStatus.COMPLETED);

            queueJobMetricService.markSuccess(queueJobMetricId);

            eventPublisher.publishEvent(new ResumeAnalysisCompleteEvent(report.getResume().getMemberId()));

        } catch (WebClientResponseException.TooManyRequests e) {
            log.warn("429 에러 발생 - Report ID: {}", reportId);
            statusManager.updateToDelayed(reportId);

            queueJobMetricService.markFailed(queueJobMetricId, e.getMessage());

        } catch (Exception e) {
            log.error("AI 분석 중 오류 - Report ID: {}", reportId, e);
            statusManager.updateToFailed(reportId);

            queueJobMetricService.markFailed(queueJobMetricId, e.getMessage());
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

    // 트랙 1: 일반 자소서 첨삭 프롬프트 (요약 생성 추가)
    private String buildNormalPrompt(String resumeContent) {
        return String.format("""
                당신은 10년 차 전문 에디터입니다. 아래 [자기소개서]의 문맥, 가독성, 표현을 다듬고 첨삭해주세요.
                [자기소개서]
                %s
                
                응답은 반드시 아래 JSON 형식으로 작성하세요.
                {
                  "overallFeedback": "전체적인 글의 흐름은 좋으나, 성과 수치가 부족합니다.",
                  "paragraphSummaries": [
                    { "paragraphNumber": 1, "summary": "직무에 대한 열정과 지원 동기" },
                    { "paragraphNumber": 2, "summary": "대용량 트래픽 처리 프로젝트 경험" }
                  ],
                  "sentenceCorrections": [
                    { "original": "열심히 했습니다", "corrected": "주도적으로 참여했습니다", "reason": "전문적인 어휘 사용" }
                  ],
                  "revisedFullContent": "전체 교정 완료된 텍스트..."
                }
                """, resumeContent);
    }

    // 트랙 2: 채용공고 기반 매칭 프롬프트 (요약 생성 추가)
    private String buildMatchPrompt(String resumeContent, String jobInfoText) {
        return String.format("""
                당신은 10년 차 수석 채용 면접관입니다. 아래 [채용 공고 상세 정보]와 [자기소개서]를 비교하여 적합도를 평가하고 첨삭해주세요.
                
                %s
                
                [자기소개서]
                %s
                
                응답은 반드시 아래 JSON 형식으로만 작성하세요. (Markdown 코드 블록 없이 순수 JSON만 반환)
                {
                  "matchingScore": 85,
                  "matchingFeedback": "직무 경험은 우수하나, 클라우드 역량 어필이 부족합니다.",
                  "keywordAnalysis": {
                     "matchedKeywords": ["Java", "Spring"],
                     "missingKeywords": ["AWS"]
                  },
                  "expectedQuestions": [
                     "꼬리 질문 1", "압박 질문 2"
                  ],
                  "overallFeedback": "전반적인 문맥은 좋으나 공고 맞춤형 수정이 필요합니다.",
                  "paragraphSummaries": [
                    { "paragraphNumber": 1, "summary": "공고의 핵심 역량에 맞춘 지원 동기" },
                    { "paragraphNumber": 2, "summary": "자격 요건을 충족하는 프로젝트 경험" }
                  ],
                  "corrections": [
                     { "original": "프로젝트를 진행했습니다.", "corrected": "AWS를 활용해 프로젝트를 배포했습니다.", "reason": "공고 요구사항 반영" }
                  ],
                  "revisedFullContent": "공고 맞춤형으로 전체 교정된 텍스트..."
                }
                
                [주의사항]
                1. 지원서의 맥락을 분석하여 각 문단(Paragraph)별로 내용을 한 줄로 요약한 소제목을 'paragraphSummaries' 배열에 작성하세요.
                """, jobInfoText, resumeContent);
    }
}