package com.aibe.team2.domain.jobposting.controller;

import com.aibe.team2.domain.auth.dto.CustomUserDetails;
import com.aibe.team2.domain.jobposting.dto.JobPostingParseResponse;
import com.aibe.team2.domain.jobposting.dto.JobPostingRequest;
import com.aibe.team2.domain.jobposting.dto.JobPostingResponse;
import com.aibe.team2.domain.jobposting.service.JobPostingParsingService;
import com.aibe.team2.domain.jobposting.service.JobPostingService;
import com.aibe.team2.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/job-postings")
@RequiredArgsConstructor
public class JobPostingController {

    private final JobPostingService jobPostingService;
    private final JobPostingParsingService jobPostingParsingService;
    // 하드코딩중 로그인 연동후 수정 // CurrentMemberId => MemberId
    // 1. 공고 등록 (URL만 줘도 내용이 채워짐)
    @PostMapping("/create")
    public ApiResponse<JobPostingResponse> createJobPosting(@RequestBody JobPostingRequest request) {
        log.info("Job Posting create requested. URL provided: {}", request.postingUrl() != null);
        Long currentMemberId = 1L;
        JobPostingResponse response = jobPostingService.createJobPosting(currentMemberId, request);
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
    public ApiResponse<List<JobPostingResponse>> getMySavedJobPostings(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        // 로그인한 사용자의 MemberId 추출
        Long memberId = 1L; // 하드코딩
        //Long memberId = userDetails.getMember().getId();
        List<JobPostingResponse> responses = jobPostingService.getMySavedJobPostings(memberId);
        return ApiResponse.success(responses);
    }

    @PostMapping("/parse")
    public ApiResponse<JobPostingParseResponse> parseJobPosting(@RequestParam("url") String url) {
        JobPostingParseResponse response = jobPostingParsingService.parseFromUrl(url);
        return ApiResponse.success(response);
    }
}
