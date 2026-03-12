package com.aibe.team2.domain.resume.controller;

import com.aibe.team2.domain.resume.dto.AnalysisMatchRequest;
import com.aibe.team2.domain.resume.service.AnalysisService;
import com.aibe.team2.domain.statistics.dto.resume.ResumeAnalysisResultResponse;
import com.aibe.team2.domain.statistics.service.ResumeStatisticsService;
import com.aibe.team2.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/resumes")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;
    private final ResumeStatisticsService resumeStatisticsService; // ★ AI 봇의 조언에 따라 추가됨

    // 1. 일반 자기소개서 첨삭 요청 (POST) - 비동기 처리 대기열 번호(reportId) 반환
    @PostMapping("/{resumeId}/analyze/general")
    public ResponseEntity<ApiResponse<Long>> analyzeGeneralResume(@PathVariable Long resumeId) {
        Long memberId = 1L; // 임시 하드코딩 (테스트용)
        Long reportId = analysisService.requestNormalAnalysis(resumeId, memberId);
        return ResponseEntity.ok(ApiResponse.success(reportId));
    }

    // 2. 채용 공고 기반 매칭 및 첨삭 요청 (POST) - 비동기 처리 대기열 번호(reportId) 반환
    @PostMapping("/{resumeId}/analyze/match")
    public ResponseEntity<ApiResponse<Long>> analyzeMatchResume(
            @PathVariable Long resumeId,
            @RequestBody AnalysisMatchRequest request) {
        Long memberId = 1L; // 임시 하드코딩 (테스트용)
        Long reportId = analysisService.requestMatchAnalysis(resumeId, memberId, request.jobPostingId());
        return ResponseEntity.ok(ApiResponse.success(reportId));
    }

    // ★ 3. 분석 결과 상세 조회 (GET) - AI 봇 조언 반영 완료!
    @GetMapping("/{resumeId}/reports/{reportId}")
    public ResponseEntity<ApiResponse<ResumeAnalysisResultResponse>> getAnalysisReport(
            @PathVariable Long resumeId,
            @PathVariable Long reportId) {

        log.info("자기소개서 ID: {} 에 대한 분석 결과 조회 요청 - 리포트 ID: {}", resumeId, reportId);
        Long memberId = 1L; // 임시 하드코딩

        // 단순히 ID만 넘기는 것이 아니라, 완성된 DTO 객체를 프론트엔드로 바로 넘겨줍니다.
        ResumeAnalysisResultResponse response = resumeStatisticsService.getResumeAnalysisReport(reportId, memberId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}