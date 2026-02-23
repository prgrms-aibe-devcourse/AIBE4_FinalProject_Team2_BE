package com.aibe.team2.domain.statistics.controller;

import com.aibe.team2.domain.statistics.dto.resume.ResumeAnalysisResultResponse;
import com.aibe.team2.domain.statistics.service.ResumeStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mypage/resumes")
public class ResumeStatisticsController {

    private final ResumeStatisticsService resumeStatisticsService;

    // [FR-REP-04] 자기소개서 첨삭 이력 - 상세 조회(리포트)
    @GetMapping("/analysis/{analysisId}")
    public ResponseEntity<ResumeAnalysisResultResponse> getResumeAnalysisReport(
            @PathVariable("analysisId") Long analysisId
    ){
        // TODO : 하드코딩 제거
        // 임시 하드코딩된 사용자 ID
        Long currentUserId = 1L;
        ResumeAnalysisResultResponse response = resumeStatisticsService.getResumeAnalysisReport(analysisId, currentUserId);

        return ResponseEntity.ok(response);
    }
}
