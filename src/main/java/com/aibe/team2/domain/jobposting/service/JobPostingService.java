package com.aibe.team2.domain.jobposting.service;

import com.aibe.team2.domain.jobposting.dto.JobPostingParseResponse;
import com.aibe.team2.domain.jobposting.dto.JobPostingRequest;
import com.aibe.team2.domain.jobposting.dto.JobPostingResponse;
import com.aibe.team2.domain.jobposting.entity.JobPosting;
import com.aibe.team2.domain.jobposting.entity.JobSkill;
import com.aibe.team2.domain.jobposting.repository.JobPostingRepository;
import com.aibe.team2.domain.resume.service.SimilarityEngine;
import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobPostingService {

    private final JobPostingRepository jobPostingRepository;
    private final JobPostingParsingService jobPostingParsingService; // 등록 시 자동 파싱용 의존성
    private final SimilarityEngine similarityEngine;
    private final ObjectMapper objectMapper;

    @Transactional
    public JobPostingResponse createJobPosting(Long memberId, JobPostingRequest request) {

        String finalCompanyName = request.companyName();
        String finalJobTitle = request.jobTitle();
        String finalDescription = request.jobDescription();
        String finalMainTasks = request.mainTasks();
        String finalQualifications = request.qualifications();
        String finalPreferred = request.preferred();
        String finalBenefits = request.benefits();
        List<String> finalRequiredSkills = request.requiredSkills();
        List<String> questionsList = request.expectedQuestions();

        // [요구사항 2] 채용 공고 등록 시 URL만 제공된 경우, ParsingService를 호출해 빈 값을 모두 채움!
        if (hasUrl(request.postingUrl()) && isEmpty(finalDescription)) {
            log.info("URL 직접 등록 요청 감지. 자동 분석 시작: {}", request.postingUrl());

            // URL만으로 크롤링 + 예상질문 파싱까지 한 번에 완료
            JobPostingParseResponse parseResult = jobPostingParsingService.autoFillFromUrl(request.postingUrl());

            // 비어있는 정보들을 AI가 가져온 데이터로 덮어쓰기
            finalCompanyName = isEmpty(finalCompanyName) ? parseResult.companyName() : finalCompanyName;
            finalJobTitle = isEmpty(finalJobTitle) ? parseResult.jobTitle() : finalJobTitle;
            finalDescription = isEmpty(finalDescription) ? parseResult.jobDescription() : finalDescription;
            finalMainTasks = isEmpty(finalMainTasks) ? parseResult.mainTasks() : finalMainTasks;
            finalQualifications = isEmpty(finalQualifications) ? parseResult.qualifications() : finalQualifications;
            finalPreferred = isEmpty(finalPreferred) ? parseResult.preferred() : finalPreferred;
            finalBenefits = isEmpty(finalBenefits) ? parseResult.benefits() : finalBenefits;

            if (finalRequiredSkills == null || finalRequiredSkills.isEmpty()) {
                finalRequiredSkills = parseResult.requiredSkills();
            }
            if (questionsList == null || questionsList.isEmpty()) {
                questionsList = parseResult.expectedQuestions();
            }
        }

        String finalExpectedQuestions = null;
        if (questionsList != null && !questionsList.isEmpty()) {
            try {
                // List<String> 형태의 질문을 DB 저장을 위해 JSON String으로 변환
                finalExpectedQuestions = objectMapper.writeValueAsString(questionsList);
            } catch (Exception e) {
                log.error("예상 질문 JSON 변환 실패", e);
            }
        }

        // 임베딩(Embedding) 추출 (복리후생 등은 제외하고 핵심 직무 내용만 벡터화)
        float[] embedding = null;
        if (!isEmpty(finalDescription)) {
            String textForEmbedding = String.join(" ",
                    finalMainTasks != null ? finalMainTasks : "",
                    finalQualifications != null ? finalQualifications : "",
                    finalPreferred != null ? finalPreferred : ""
            ).trim();

            if (!textForEmbedding.isEmpty()) {
                embedding = similarityEngine.getEmbeddingAsFloatArray(textForEmbedding);
            }
        }

        // Entity 생성 및 저장
        JobPosting jobPosting = JobPosting.builder()
                .memberId(memberId)
                .companyName(finalCompanyName)
                .jobTitle(finalJobTitle)
                .postingUrl(request.postingUrl())
                .jobDescription(finalDescription)
                .mainTasks(finalMainTasks)
                .qualifications(finalQualifications)
                .preferred(finalPreferred)
                .benefits(finalBenefits)
                .expectedQuestions(finalExpectedQuestions)
                .embedding(embedding)
                .build();

        // 스킬 매핑
        if (finalRequiredSkills != null && !finalRequiredSkills.isEmpty()) {
            finalRequiredSkills.forEach(skillName -> {
                JobSkill jobSkill = JobSkill.builder().jobPosting(jobPosting).skillName(skillName).build();
                jobPosting.addJobSkill(jobSkill);
            });
        }

        JobPosting savedPosting = jobPostingRepository.save(jobPosting);
        return JobPostingResponse.from(savedPosting);
    }

    public JobPostingResponse getJobPosting(Long id) {
        JobPosting jobPosting = jobPostingRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_404));
        return JobPostingResponse.from(jobPosting);
    }

    public List<JobPostingResponse> getMySavedJobPostings(Long memberId) {
        return jobPostingRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId).stream()
                .map(JobPostingResponse::from).collect(Collectors.toList());
    }

    private boolean hasUrl(String url) {
        return url != null && !url.isBlank() && (url.startsWith("http") || url.startsWith("https"));
    }

    private boolean isEmpty(String text) {
        return text == null || text.isBlank();
    }
}