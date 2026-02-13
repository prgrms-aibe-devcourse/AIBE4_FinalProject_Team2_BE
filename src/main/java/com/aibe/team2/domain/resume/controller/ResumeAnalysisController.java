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
@RequestMapping("/api/resumes") // URL 구조 유지를 위해 동일한 path 사용
@RequiredArgsConstructor
public class ResumeAnalysisController {

    private final ResumeAnalysisService resumeAnalysisService;

    // 1. 이력서 분석 요청 (AI 사용)
    // [POST] /api/resumes/{resumeId}/analysis
    @PostMapping("/{resumeId}/analysis")
    public ApiResponse<Long> analyzeResume(@PathVariable Long resumeId) {
        log.info("resumeId에 대한 분석 요청: {}", resumeId);
        Long reportId = resumeAnalysisService.analyzeResume(resumeId);
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