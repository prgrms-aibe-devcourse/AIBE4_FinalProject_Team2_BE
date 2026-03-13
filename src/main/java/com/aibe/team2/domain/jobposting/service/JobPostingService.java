package com.aibe.team2.domain.jobposting.service;

import com.aibe.team2.domain.jobposting.dto.JobPostingRequest;
import com.aibe.team2.domain.jobposting.dto.JobPostingResponse;
import com.aibe.team2.domain.jobposting.entity.JobPosting;
import com.aibe.team2.domain.jobposting.entity.JobSkill;
import com.aibe.team2.domain.jobposting.repository.JobPostingRepository;
import com.aibe.team2.domain.resume.service.SimilarityEngine;
import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobPostingService {

    private final JobPostingRepository jobPostingRepository;
    private final SimilarityEngine similarityEngine;
    private final JobPostingCrawlerService jobPostingCrawlerService;

    @Transactional
    public JobPostingResponse createJobPosting(Long memberId, JobPostingRequest request) {

        String finalDescription = request.jobDescription();
        String finalJobTitle = request.jobTitle();
        String finalMainTasks = request.mainTasks();
        String finalQualifications = request.qualifications();
        String finalPreferred = request.preferred();
        String finalBenefits = request.benefits();

        // 2. URL 크롤링 및 파싱
        if (hasUrl(request.postingUrl()) && isEmpty(finalDescription)) {
            log.info("Crawling requested for URL: {}", request.postingUrl());

            Map<String, String> parsedData = jobPostingCrawlerService.crawlAndParse(request.postingUrl());

            if (!parsedData.isEmpty()) {
                finalDescription = parsedData.getOrDefault("fullDescription", finalDescription);
                if (isEmpty(finalJobTitle)) finalJobTitle = parsedData.getOrDefault("title", finalJobTitle);

                // 사용자 입력값이 비어있는 경우에만 크롤링 데이터로 채움
                if (isEmpty(finalMainTasks)) finalMainTasks = parsedData.get("mainTasks");
                if (isEmpty(finalQualifications)) finalQualifications = parsedData.get("qualifications");
                if (isEmpty(finalPreferred)) finalPreferred = parsedData.get("preferred");
                if (isEmpty(finalBenefits)) finalBenefits = parsedData.get("benefits");

                log.info("파싱 완료 - 주요업무: {}, 자격요건: {}", finalMainTasks != null, finalQualifications != null);
            }
        }

        // 3. 공고 내용 텍스트를 벡터(Embedding)로 변환
        float[] embedding = null;
        if (!isEmpty(finalDescription)) {
            embedding = similarityEngine.getEmbeddingAsFloatArray(finalDescription);
        }

        // 4. Entity 생성 및 저장
        JobPosting jobPosting = JobPosting.builder()
                .memberId(memberId)
                .companyName(request.companyName())
                .jobTitle(request.jobTitle())
                .postingUrl(request.postingUrl())
                .jobDescription(request.jobDescription())
                .mainTasks(request.mainTasks())
                .qualifications(request.qualifications())
                .preferred(request.preferred())
                .benefits(request.benefits())
                .embedding(embedding)
                .build();

        // 💡
        if (request.requiredSkills() != null && !request.requiredSkills().isEmpty()) {
            request.requiredSkills().forEach(skillName -> {
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