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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobPostingService {

    private final JobPostingRepository jobPostingRepository;
    private final SimilarityEngine similarityEngine; // 임베딩 API 호출용 추가

    @Transactional
    public JobPostingResponse createJobPosting(Long memberId, JobPostingRequest request) {
        String finalDescription = request.jobDescription();
        String finalJobTitle = request.jobTitle();

        // 1. URL 크롤링
        if (hasUrl(request.postingUrl()) && isEmpty(finalDescription)) {
            log.info("Crawling requested for URL: {}", request.postingUrl());

            try {
                Document doc = Jsoup.connect(request.postingUrl())
                        .userAgent("Mozilla/5.0")
                        .timeout(5000).get();
                finalDescription = doc.body().text();
                if (isEmpty(finalJobTitle)) finalJobTitle = doc.title();
            } catch (IOException e) {
                log.error("Failed to crawl URL: {}", request.postingUrl(), e);

            }
        }

        // 2. 공고 내용 텍스트를 벡터(Embedding)로 변환
        float[] embedding = similarityEngine.getEmbeddingAsFloatArray(finalDescription);

        // 3. Entity 생성
        JobPosting jobPosting = JobPosting.builder()
                .memberId(memberId)
                .companyName(request.companyName())
                .jobTitle(finalJobTitle)
                .postingUrl(request.postingUrl())
                .jobDescription(finalDescription)
                .embedding(embedding) // 추출한 벡터값 삽입
                .build();

        // 4. 스킬 목록 생성
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
        JobPosting jobPosting = jobPostingRepository.findById(id).orElseThrow(() -> new BusinessException(ErrorCode.COMMON_404));
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