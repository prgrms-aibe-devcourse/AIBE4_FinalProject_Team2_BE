package com.aibe.team2.domain.jobposting.service;

import com.aibe.team2.domain.jobposting.dto.JobPostingRequest;
import com.aibe.team2.domain.jobposting.dto.JobPostingResponse;
import com.aibe.team2.domain.jobposting.entity.JobPosting;
import com.aibe.team2.domain.jobposting.repository.JobPostingRepository;
import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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

    @Transactional
    public JobPostingResponse createJobPosting(Long memberId, JobPostingRequest request) {
        String url = request.postingUrl();

        if (!hasUrl(url)) {
            throw new IllegalArgumentException("올바른 공고 URL을 입력해주세요.");
        }

        String companyName = request.companyName();
        String jobTitle = request.jobTitle();

        if (companyName == null || companyName.isBlank() || jobTitle == null || jobTitle.isBlank()) {
            try {
                Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(5000).get();
                String pageTitle = doc.title();
                Element ogTitle = doc.selectFirst("meta[property=og:title]");
                if (ogTitle != null && !ogTitle.attr("content").isBlank()) {
                    pageTitle = ogTitle.attr("content");
                }
                if (companyName == null || companyName.isBlank()) companyName = extractCompanyName(pageTitle);
                if (jobTitle == null || jobTitle.isBlank()) jobTitle = extractJobTitle(pageTitle);
            } catch (IOException e) {
                if (companyName == null || companyName.isBlank()) companyName = "미상";
                if (jobTitle == null || jobTitle.isBlank()) jobTitle = "제목 없음";
            }
        }


        float[] emptyEmbedding = new float[768];

        JobPosting jobPosting = JobPosting.builder()
                .memberId(memberId)
                .companyName(companyName)
                .jobTitle(jobTitle)
                .postingUrl(url)
                .jobDescription("")
                .embedding(emptyEmbedding)
                .build();

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

    // --- 제목 파싱 헬퍼 메서드 ---
    private String extractCompanyName(String title) {
        if (title == null) return "미상";
        // 예: "[카카오뱅크] 백엔드" -> "카카오뱅크"
        if (title.startsWith("[")) {
            int endIdx = title.indexOf("]");
            if (endIdx > 0) return title.substring(1, endIdx).trim();
        }
        // 예: "카카오뱅크 - 서버 개발자" -> "카카오뱅크"
        if (title.contains("-")) return title.split("-")[0].trim();

        return title.split(" ")[0].trim(); // 아무 패턴이 없으면 첫 번째 단어 반환
    }

    private String extractJobTitle(String title) {
        if (title == null) return "제목 없음";
        String cleaned = title;

        // 예: "[카카오뱅크] 백엔드 개발자" -> "백엔드 개발자"
        if (title.startsWith("[")) {
            int endIdx = title.indexOf("]");
            if (endIdx > 0) cleaned = title.substring(endIdx + 1).trim();
        } else if (title.contains("-")) {
            String[] parts = title.split("-");
            if (parts.length > 1) cleaned = parts[1].trim();
        }

        // 뒤에 붙는 "| 원티드", "채용 | 사람인" 등 꼬리표 제거
        if (cleaned.contains("|")) {
            cleaned = cleaned.split("\\|")[0].trim();
        }
        return cleaned.isEmpty() ? title : cleaned;
    }
}