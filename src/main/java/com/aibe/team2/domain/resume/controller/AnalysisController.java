package com.aibe.team2.domain.resume.controller;

import com.aibe.team2.domain.resume.dto.AnalysisMatchRequest;
import com.aibe.team2.domain.resume.service.AnalysisService;
import com.aibe.team2.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/resumes")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    // 1. 일반 자기소개서 첨삭 요청
    @PostMapping("/{resumeId}/analyze/normal")
    public ResponseEntity<ApiResponse<Long>> analyzeNormalResume(@PathVariable Long resumeId) {
        Long memberId = 1L; // 임시 하드코딩 (테스트용)
        Long reportId = analysisService.requestNormalAnalysis(resumeId, memberId);
        return ResponseEntity.ok(ApiResponse.success(reportId));
    }

    // 2. 채용 공고 기반 매칭 및 첨삭 요청
    @PostMapping("/{resumeId}/analyze/match")
    public ResponseEntity<ApiResponse<Long>> analyzeMatchResume(
            @PathVariable Long resumeId,
            @RequestBody AnalysisMatchRequest request) {
        Long memberId = 1L; // 임시 하드코딩 (테스트용)
        Long reportId = analysisService.requestMatchAnalysis(resumeId, memberId, request.jobPostingId());
        return ResponseEntity.ok(ApiResponse.success(reportId));
    }

    // 3. 요청 한 자기소개서 조회
    @GetMapping("/{resumeId}/analysis")
    public ResponseEntity<ApiResponse<Long>> getAnalysisReport(@PathVariable Long resumeId) {
        Long memberId = 1L; // 임시 하드코딩 (테스트용)
        Long reportId = analysisService.getAnalysisReport(resumeId, memberId);
        return ResponseEntity.ok(ApiResponse.success(reportId));
    }

}