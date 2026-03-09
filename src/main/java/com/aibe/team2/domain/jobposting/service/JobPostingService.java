package com.aibe.team2.domain.jobposting.service;

import com.aibe.team2.domain.jobposting.dto.JobPostingRequest;
import com.aibe.team2.domain.jobposting.dto.JobPostingResponse;
import com.aibe.team2.domain.jobposting.entity.JobPosting;
import com.aibe.team2.domain.jobposting.entity.JobSkill;
import com.aibe.team2.domain.jobposting.repository.JobPostingRepository;
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

    // 공고 등록 (URL 크롤링 기능 포함)
    @Transactional
    public JobPostingResponse createJobPosting(Long memberId, JobPostingRequest request) {
        String finalDescription = request.jobDescription();
        String finalJobTitle = request.jobTitle();

        // 1. URL이 있고, 본문 내용이 비어있다면 크롤링 시도
        if (hasUrl(request.postingUrl()) && isEmpty(finalDescription)) {
            log.info("Crawling requested for URL: {}", request.postingUrl());
            try {
                Document doc = Jsoup.connect(request.postingUrl())
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                        .timeout(5000)
                        .get();

                String crawledText = doc.body().text();
                finalDescription = crawledText;
                log.info("Crawling success. Text length: {}", crawledText.length());

                if (isEmpty(finalJobTitle)) {
                    finalJobTitle = doc.title();
                }

            } catch (IOException e) {
                log.error("Failed to crawl URL: {}", request.postingUrl(), e);
            }
        }

        // 2. Entity 생성 (JobPosting)
        JobPosting jobPosting = JobPosting.builder()
                .memberId(memberId)
                .companyName(request.companyName())
                .jobTitle(finalJobTitle)
                .postingUrl(request.postingUrl())
                .jobDescription(finalDescription)
                .build();

        // 3. 스킬 목록(JobSkill) 엔티티 생성
        if (request.requiredSkills() != null && !request.requiredSkills().isEmpty()) {
            request.requiredSkills().forEach(skillName -> {
                JobSkill jobSkill = JobSkill.builder()
                        .jobPosting(jobPosting) // 연관관계 설정
                        .skillName(skillName)
                        .build();

                // 편의 메서드를 통해 양방향 매핑 설정
                jobPosting.addJobSkill(jobSkill);
            });
        }

        // 4. 저장 (JobPosting 엔티티에 cascade = CascadeType.ALL이 설정되어 있으므로 JobSkill도 함께 DB에 저장됨)
        JobPosting savedPosting = jobPostingRepository.save(jobPosting);

        return JobPostingResponse.from(savedPosting);
    }

    public JobPostingResponse getJobPosting(Long id) {
        JobPosting jobPosting = jobPostingRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_404));
        return JobPostingResponse.from(jobPosting);
    }

    public List<JobPostingResponse> getMySavedJobPostings(Long memberId) {
        // 하드코딩중
        // Long memberId = 1L;
        return jobPostingRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId).stream()
                .map(JobPostingResponse::from)
                .collect(Collectors.toList());
    }

    // Helper Methods
    private boolean hasUrl(String url) {
        return url != null && !url.isBlank() && (url.startsWith("http") || url.startsWith("https"));
    }

    private boolean isEmpty(String text) {
        return text == null || text.isBlank();
    }
}