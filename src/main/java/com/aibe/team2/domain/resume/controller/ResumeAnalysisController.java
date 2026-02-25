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

    // 1. 자소서 분석 요청 (AI 사용)
    // [POST] /api/resumes/{resumeId}/analysis?jobPostingId={jobPostingId}
    @PostMapping("/{resumeId}/analysis")
    public ApiResponse<Long> analyzeResume(
            @PathVariable Long resumeId,
            @RequestParam Long jobPostingId // ✅ 쿼리 파라미터로 jobPostingId를 동적으로 받습니다.
    ) {
        log.info("이력서 ID: {}, 채용공고 ID: {} 에 대한 분석 요청", resumeId, jobPostingId);
        Long reportId = resumeAnalysisService.analyzeResume(resumeId, jobPostingId);
        return ApiResponse.success(reportId);
    }

    // 2. 분석 결과 조회
    // [GET] /api/resumes/{resumeId}/analysis
    @GetMapping("/{resumeId}/analysis")
    public ApiResponse<ResumeAnalysisResponse> getAnalysisResult(@PathVariable Long resumeId) {
        ResumeAnalysisReport report = resumeAnalysisService.getAnalysisResult(resumeId);
        return ApiResponse.success(ResumeAnalysisResponse.from(report));
    }
}