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
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.weaver.ast.Not;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeAnalysisService {

    private final ResumeRepository resumeRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    // [추가] 레포지토리 의존성 주입
    private final JobPostingRepository jobPostingRepository;
    // [추가] JSON 변환
    private final ObjectMapper objectMapper;

    /**
     * 이력서 분석 요청 (Upsert Logic)
     * 유니크 제약조건(resume_id + job_posting_id)을 고려하여
     * 기존 분석 내역이 있으면 갱신하고, 없으면 새로 생성합니다.
     */
    @Transactional
    public Long analyzeResume(Long resumeId) {
        // 1. 이력서 조회
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RESUME_NOT_FOUND));

        // TODO: 추후 API 파라미터로 jobPostingId를 받아야 함. 현재는 1L(임시) 고정
        Long defaultJobPostingId = 1L;

        // [추가] JobPosting 엔티티 조회
        JobPosting jobPosting = jobPostingRepository.findById(defaultJobPostingId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COMMON_404));

        // 2. 기존 분석 이력 조회 (유니크 충돌 방지)
        // Repository에 findByResumeIdAndJobPostingId 메서드가 있다고 가정하거나,
        // 현재는 findTopBy...를 사용하여 최신건을 가져와 비교합니다.
        Optional<ResumeAnalysisReport> existingReport = resumeAnalysisRepository
                .findTopByResumeIdOrderByCreatedAtDesc(resumeId);

        ResumeAnalysisReport report;

        if (existingReport.isPresent() && existingReport.get().getJobPostingId().getId().equals(defaultJobPostingId)) {
            // 2-1. 이미 존재하는 리포트 -> 재분석 (Update)
            report = existingReport.get();
            report.startAnalysis(); // 상태를 PROCESSING으로 변경 및 갱신
            log.info("Existing report found. Restarting analysis for reportId: {}", report.getId());
        } else {
            // 2-2. 새로운 리포트 생성 (Create)
            report = ResumeAnalysisReport.builder()
                    .resume(resume)
                    .jobPostingId(jobPosting) // NOT NULL 제약조건 준수
                    .build();
            report.startAnalysis(); // PROCESSING
            log.info("New analysis report created for resumeId: {}", resumeId);
        }

        // 3. 저장 (Insert or Update)
        ResumeAnalysisReport savedReport = resumeAnalysisRepository.save(report);

        // 4. AI 분석 실행 (Mock)
        try {
            // 실제 구현 시 @Async 비동기 처리 권장
            AiAnalysisResult result = mockAiCall(resume.getContent());

            // [추가] String(JSON) -> Map<String, Object> (역직렬화)
            Map<String, Object> subtitleMap = objectMapper.readValue(result.generatedSubtitle(), new TypeReference<>(){});
            Map<String, Object> keywordsMap = objectMapper.readValue(result.keywords(), new TypeReference<>(){});
            Map<String, Object> correctionsMap = objectMapper.readValue(result.corrections(), new TypeReference<>(){});

            // 5. 분석 성공 처리 (데이터 업데이트)
            savedReport.completeAnalysis(
                    result.score,
                    subtitleMap, // [수정]
                    keywordsMap,          // [수정]
                    correctionsMap,       // [수정]
                    result.revisedContent
            );
            // [추가] 역직렬화(JSON 변환) 실패 시
        } catch(JsonProcessingException e) {
            log.error("JSON parsing failed for resumeId: {}", resumeId, e);
            savedReport.failAnalysis();

        } catch (Exception e) {
            log.error("AI Analysis failed for resumeId: {}", resumeId, e);
            savedReport.failAnalysis();
        }

        return savedReport.getId();
    }

    /**
     * 분석 결과 조회
     */
    @Transactional(readOnly = true)
    public ResumeAnalysisReport getAnalysisResult(Long resumeId) {
        return resumeAnalysisRepository.findTopByResumeIdOrderByCreatedAtDesc(resumeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COMMON_404));
    }

    // --- (Internal) AI Mock Method ---
    // 실제 OpenAI API 연동 전, DB 스키마에 맞는 더미 데이터를 생성합니다.
    private AiAnalysisResult mockAiCall(String content) {
        log.info("Calling Mock AI for content length: {}", (content != null ? content.length() : 0));

        // 1. 소제목 (JSON)
        String mockSubtitleJson = """
                {
                    "title": "데이터 분석 역량을 갖춘 백엔드 개발자",
                    "reason": "프로젝트 경험에서 데이터 처리 능력이 돋보입니다."
                }
                """;

        // 2. 키워드 분석 (JSON)
        // [수정] JSON 배열에서 객체 형태로 변경
        // String mockKeywordsJson = """
        //         [
        //             {"keyword": "Spring Boot", "count": 5, "importance": "HIGH"},
        //             {"keyword": "JPA", "count": 3, "importance": "MEDIUM"},
        //             {"keyword": "AWS", "count": 1, "importance": "LOW"}
        //         ]
        //         """;
        String mockKeywordsJson = """
                {
                    "keywords": [
                        {"keyword": "Spring Boot", "count": 5, "importance": "HIGH"},
                        {"keyword": "JPA", "count": 3, "importance": "MEDIUM"},
                        {"keyword": "AWS", "count": 1, "importance": "LOW"}
                    ]
                }
                """;

        // 3. 문장 교정 (JSON)
        // String mockCorrectionsJson = """
        //         [
        //             {
        //                 "original": "열심히 했습니다.",
        //                 "corrected": "주도적으로 프로젝트를 리딩하여 성과를 냈습니다.",
        //                 "reason": "구체적인 성과 위주로 서술하는 것이 좋습니다."
        //             }
        //         ]
        //         """;
        String mockCorrectionsJson = """
                {
                    "corrections": [
                        {
                            "original": "열심히 했습니다.",
                            "corrected": "주도적으로 프로젝트를 리딩하여 성과를 냈습니다.",
                            "reason": "구체적인 성과 위주로 서술하는 것이 좋습니다."
                        }
                    ]
                }
                """;

        // 4. 첨삭 완료 본문
        String mockRevisedContent = "주도적으로 프로젝트를 리딩하여 성과를 냈습니다... (AI 첨삭 내용)";

        return new AiAnalysisResult(
                85,                 // matchScore
                mockSubtitleJson,   // generatedSubtitle
                mockKeywordsJson,   // keywords
                mockCorrectionsJson,// corrections
                mockRevisedContent  // revisedFullContent
        );
    }

    // 내부 데이터 전달용 DTO (Record)
    private record AiAnalysisResult(
            Integer score,
            String generatedSubtitle,
            String keywords,
            String corrections,
            String revisedContent
    ) {}
}