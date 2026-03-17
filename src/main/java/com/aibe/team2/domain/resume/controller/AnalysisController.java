package com.aibe.team2.domain.resume.controller;

import com.aibe.team2.domain.resume.dto.AnalysisMatchRequest;
import com.aibe.team2.domain.resume.dto.AnalysisResponse;
import com.aibe.team2.domain.resume.service.AnalysisService;
import com.aibe.team2.global.common.annotation.LoginMemberId;
import com.aibe.team2.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "자소서 분석(Analysis)", description = "AI 자소서 첨삭 및 매칭 분석 API")
@Slf4j
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

    // 3. 분석 결과 상세 조회 (GET)
    @GetMapping("/{resumeId}/reports/{reportId}")
    public ResponseEntity<ApiResponse<AnalysisResponse>> getAnalysisReport(
            @PathVariable Long resumeId,
            @PathVariable Long reportId) {

        log.info("자기소개서 ID: {} 에 대한 분석 결과 원본 상세 조회 요청 - 리포트 ID: {}", resumeId, reportId);
        Long memberId = 1L; // 임시 하드코딩
        AnalysisResponse response = analysisService.getAnalysisResult(resumeId, reportId, memberId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // 4. 분석 재시도
    @Operation(summary = "분석 재시도", description = "실패(FAILED)하거나 지연(DELAYED)된 분석을 재시도합니다.")
    @PostMapping("/{reportId}/retry")
    public ApiResponse<Void> retryAnalysis(
            @PathVariable Long reportId,
            @LoginMemberId Long memberId) {

        analysisService.retryAnalysis(reportId, memberId);

        // return ApiResponse.success(reportId);
        return ApiResponse.success(null);
    }
}