package com.aibe.team2.domain.resume.controller;

import com.aibe.team2.domain.resume.dto.ResumeAnalysisResponse;
import com.aibe.team2.domain.resume.entity.ResumeAnalysisReport;
import com.aibe.team2.domain.resume.service.ResumeAnalysisService;
import com.aibe.team2.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeAnalysisController {

    private final ResumeAnalysisService resumeAnalysisService;

    // TODO: 나중에 Spring Security 로그인 연동되면 지우고 @AuthenticationPrincipal 쓸 임시 메서드
    private Long getLoginMemberId() {
        return 1L;
    }

    @PostMapping("/{resumeId}/analysis")
    public ApiResponse<Long> analyzeResume(
            @PathVariable Long resumeId,
            @RequestParam Long jobPostingId
    ) {
        Long memberId = getLoginMemberId();

        log.info("유저 ID: {}, 이력서 ID: {}, 채용공고 ID: {} 에 대한 분석 요청", memberId, resumeId, jobPostingId);

        Long reportId = resumeAnalysisService.analyzeResume(resumeId, jobPostingId, memberId);
        return ApiResponse.success(reportId);
    }

    @GetMapping("/{resumeId}/analysis")
    public ApiResponse<ResumeAnalysisResponse> getAnalysisResult(@PathVariable Long resumeId) {
        log.info("이력서 ID: {} 에 대한 분석 결과 조회 요청", resumeId);

        ResumeAnalysisReport report = resumeAnalysisService.getAnalysisResult(resumeId);
        return ApiResponse.success(ResumeAnalysisResponse.from(report));
    }
}