package com.aibe.team2.domain.jobposting.controller;

import com.aibe.team2.domain.jobposting.dto.JobPostingRequest;
import com.aibe.team2.domain.jobposting.dto.JobPostingResponse;
import com.aibe.team2.domain.jobposting.service.JobPostingService;
import com.aibe.team2.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/job-postings")
@RequiredArgsConstructor
public class JobPostingController {

    private final JobPostingService jobPostingService;

    // 1. 공고 등록 (URL만 줘도 내용이 채워짐)
    @PostMapping
    public ApiResponse<JobPostingResponse> createJobPosting(@RequestBody @Valid JobPostingRequest request) {
        log.info("Job Posting create requested. URL provided: {}", request.postingUrl() != null);
        JobPostingResponse response = jobPostingService.createJobPosting(request);
        return ApiResponse.success(response);
    }

    // 2. 상세 조회
    @GetMapping("/{id}")
    public ApiResponse<JobPostingResponse> getJobPosting(@PathVariable Long id) {
        JobPostingResponse response = jobPostingService.getJobPosting(id);
        return ApiResponse.success(response);
    }

    // 3. 내 관심 공고 목록 조회
    @GetMapping
    public ApiResponse<List<JobPostingResponse>> getMySavedJobPostings(@RequestParam Long memberId) {
        List<JobPostingResponse> responses = jobPostingService.getMySavedJobPostings(memberId);
        return ApiResponse.success(responses);
    }
}