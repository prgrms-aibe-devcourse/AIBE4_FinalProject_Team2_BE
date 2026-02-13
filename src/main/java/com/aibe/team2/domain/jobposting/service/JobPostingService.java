package com.aibe.team2.domain.jobposting.service;

import com.aibe.team2.domain.jobposting.dto.JobPostingRequest;
import com.aibe.team2.domain.jobposting.dto.JobPostingResponse;
import com.aibe.team2.domain.jobposting.entity.JobPosting;
import com.aibe.team2.domain.jobposting.repository.JobPostingRepository;
import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.custom.NotFoundException;
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
    public JobPostingResponse createJobPosting(JobPostingRequest request) {
        String finalDescription = request.jobDescription();
        String finalJobTitle = request.jobTitle();

        // 1. URL이 있고, 본문 내용이 비어있다면 크롤링 시도
        if (hasUrl(request.postingUrl()) && isEmpty(finalDescription)) {
            log.info("Crawling requested for URL: {}", request.postingUrl());
            try {
                // Jsoup 연결 및 문서 가져오기 (User-Agent 설정으로 403 방지)
                Document doc = Jsoup.connect(request.postingUrl())
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                        .timeout(5000) // 5초 타임아웃
                        .get();

                // 페이지의 모든 텍스트 추출 (HTML 태그 제거)
                // 만약 특정 사이트(원티드 등)만 타겟팅한다면 doc.select(".class-name").text()로 정교하게 가능
                String crawledText = doc.body().text();

                // 크롤링한 내용으로 덮어쓰기
                finalDescription = crawledText;
                log.info("Crawling success. Text length: {}", crawledText.length());

                // (선택) 제목이 비어있으면 페이지 타이틀(<title>) 가져오기
                if (isEmpty(finalJobTitle)) {
                    finalJobTitle = doc.title();
                }

            } catch (IOException e) {
                log.error("Failed to crawl URL: {}", request.postingUrl(), e);
                // 크롤링 실패해도 에러를 던지지 않고, 입력된 내용(비어있을 수 있음)으로 진행
            }
        }

        // 2. Entity 생성
        JobPosting jobPosting = JobPosting.builder()
                .userId(request.userId())
                .companyName(request.companyName())
                .jobTitle(finalJobTitle)
                .postingUrl(request.postingUrl()) // URL 저장
                .jobDescription(finalDescription) // 크롤링된 결과 or 원본
                .requiredSkills(request.requiredSkills())
                .build();

        // 3. 저장
        JobPosting savedPosting = jobPostingRepository.save(jobPosting);
        return JobPostingResponse.from(savedPosting);
    }

    public JobPostingResponse getJobPosting(Long id) {
        JobPosting jobPosting = jobPostingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COMMON_404));
        return JobPostingResponse.from(jobPosting);
    }

    public List<JobPostingResponse> getMySavedJobPostings(Long userId) {
        return jobPostingRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream()
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