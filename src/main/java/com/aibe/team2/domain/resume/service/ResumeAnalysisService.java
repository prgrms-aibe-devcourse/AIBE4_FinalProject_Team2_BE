package com.aibe.team2.domain.resume.service;

import com.aibe.team2.domain.jobposting.entity.JobPosting;
import com.aibe.team2.domain.jobposting.repository.JobPostingRepository;
import com.aibe.team2.domain.resume.entity.Resume;
import com.aibe.team2.domain.resume.entity.ResumeAnalysisReport;
import com.aibe.team2.domain.resume.repository.ResumeAnalysisRepository;
import com.aibe.team2.domain.resume.repository.ResumeRepository;
import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.custom.NotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeAnalysisService {

    private final ResumeRepository resumeRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final JobPostingRepository jobPostingRepository;
    private final ObjectMapper objectMapper;

    // 외부 API 비동기/동기 호출을 위한 WebClient
    private final WebClient.Builder webClientBuilder;

    // Gemini API 키와 URL 주입
    @Value("${gemini.api.key}")
    private String geminiApiKey;

    @Value("${gemini.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-3.0-flash:generateContent}")
    private String geminiApiUrl;

    /**
     * 이력서 분석 요청 (Upsert Logic)
     */
    @Transactional
    public Long analyzeResume(Long resumeId) {
        // 1. 이력서 조회
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RESUME_NOT_FOUND));

        // TODO: 추후 API 파라미터로 jobPostingId를 받아야 함. 현재는 1L(임시) 고정
        Long defaultJobPostingId = 1L;
        JobPosting jobPosting = jobPostingRepository.findById(defaultJobPostingId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COMMON_404));

        // 2. 기존 분석 이력 조회 (유니크 충돌 방지)
        Optional<ResumeAnalysisReport> existingReport = resumeAnalysisRepository
                .findTopByResumeIdOrderByCreatedAtDesc(resumeId);

        ResumeAnalysisReport report;
        if (existingReport.isPresent() && existingReport.get().getJobPostingId().getId().equals(defaultJobPostingId)) {
            report = existingReport.get();
            report.startAnalysis(); // 상태를 PROCESSING으로 변경 및 갱신
            log.info("Existing report found. Restarting analysis for reportId: {}", report.getId());
        } else {
            report = ResumeAnalysisReport.builder()
                    .resume(resume)
                    .jobPostingId(jobPosting)
                    .build();
            report.startAnalysis();
            log.info("New analysis report created for resumeId: {}", resumeId);
        }

        ResumeAnalysisReport savedReport = resumeAnalysisRepository.save(report);

        // 4. 실제 Gemini AI 분석 실행
        try {
            // 이력서 내용과 직무 내용을 모두 넘겨주어 분석 정확도를 높임
            AiAnalysisResult result = callGeminiApi(resume.getContent(), jobPosting.getJobDescription());

            // 5. 분석 성공 처리 (데이터 업데이트)
            savedReport.completeAnalysis(
                    result.score(),
                    result.generatedSubtitle(),
                    result.keywords(),
                    result.corrections(),
                    result.revisedContent()
            );
        } catch (Exception e) {
            log.error("AI Analysis failed for resumeId: {}", resumeId, e);
            savedReport.failAnalysis();
        }

        return savedReport.getId();
    }

    @Transactional(readOnly = true)
    public ResumeAnalysisReport getAnalysisResult(Long resumeId) {
        return resumeAnalysisRepository.findTopByResumeIdOrderByCreatedAtDesc(resumeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COMMON_404));
    }

    // --- 실전 Gemini API 호출 메서드 ---
    private AiAnalysisResult callGeminiApi(String resumeContent, String jobDescription) throws JsonProcessingException {
        log.info("Calling Gemini 3.0 Flash API...");

        // Java 15+ Text Block을 활용한 깔끔한 프롬프트 작성
        // ResumeStatisticsService에서 기대하는 JSON 스펙과 정확히 일치시킵니다.
        String prompt = String.format("""
                당신은 10년 차 전문 채용 담당자이자 이력서 첨삭 전문가입니다.
                아래의 [채용 공고]와 지원자의 [이력서]를 분석하고, 합격률을 높일 수 있도록 이력서를 첨삭해주세요.
                
                [채용 공고]
                %s
                
                [이력서]
                %s
                
                응답은 반드시 아래의 JSON 형식으로만 작성해야 하며, 마크다운(```json 등)이나 부가 설명은 절대 포함하지 마세요.
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

        // Gemini API Request Body 구성
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                ),
                "generationConfig", Map.of(
                        // 중요: Gemini가 순수 JSON 포맷으로만 응답하도록 강제
                        "responseMimeType", "application/json"
                )
        );

        WebClient webClient = webClientBuilder.baseUrl(geminiApiUrl).build();

        // API 동기 호출 (비즈니스 로직이 동기식으로 짜여 있으므로 block() 사용)
        JsonNode responseNode = webClient.post()
                .uri(uriBuilder -> uriBuilder.queryParam("key", geminiApiKey).build())
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (responseNode == null || !responseNode.has("candidates")) {
            throw new RuntimeException("Gemini API 응답이 비어있거나 형식이 올바르지 않습니다.");
        }

        // Gemini가 생성한 JSON 텍스트 추출
        String responseText = responseNode.get("candidates").get(0)
                .get("content").get("parts").get(0).get("text").asText();

        // 문자열 형태의 JSON을 JsonNode로 1차 파싱
        JsonNode parsedResult = objectMapper.readTree(responseText);

        // AiAnalysisResult에 바로 Map 객체로 변환하여 할당 (이중 파싱 방지)
        return new AiAnalysisResult(
                parsedResult.get("score").asInt(),
                objectMapper.convertValue(parsedResult.get("generatedSubtitle"), new TypeReference<Map<String, Object>>() {}),
                objectMapper.convertValue(parsedResult.get("keywords"), new TypeReference<Map<String, Object>>() {}),
                objectMapper.convertValue(parsedResult.get("corrections"), new TypeReference<Map<String, Object>>() {}),
                parsedResult.get("revisedFullContent").asText()
        );
    }

    // 내부 데이터 전달용 DTO
    // 기존 String 타입들을 Map으로 변경하여 코드 복잡도를 낮췄습니다.
    private record AiAnalysisResult(
            Integer score,
            Map<String, Object> generatedSubtitle,
            Map<String, Object> keywords,
            Map<String, Object> corrections,
            String revisedContent
    ) {}
}