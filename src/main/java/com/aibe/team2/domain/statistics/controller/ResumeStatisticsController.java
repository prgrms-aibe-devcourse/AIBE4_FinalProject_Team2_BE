package com.aibe.team2.domain.statistics.controller;

import com.aibe.team2.domain.statistics.dto.resume.ResumeAnalysisListResponse;
import com.aibe.team2.domain.statistics.dto.resume.ResumeAnalysisResultResponse;
import com.aibe.team2.domain.statistics.service.ResumeStatisticsService;
import com.aibe.team2.global.redis.ratelimit.RateLimit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mypage/resumes")
public class ResumeStatisticsController {

    private final ResumeStatisticsService resumeStatisticsService;

    // [FR-REP-04] 자기소개서 첨삭 이력 조회
    @RateLimit
    @GetMapping("/analysis")
    public ResponseEntity<Page<ResumeAnalysisListResponse>> getResumeAnalysisList(
            @RequestParam(defaultValue = "0") int page, // URL 파라미터: 시작 페이지 (기본 0)
            @RequestParam(defaultValue = "10") int size // URL 파라미터: 페이지당 개수 (기본 10개)
    ) {
        // TODO : Spring Security 연동 시 수정
        Long currentUserId = 1L;
        // 최신순으로 정렬하는 페이징 객체 생성
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ResumeAnalysisListResponse> response = resumeStatisticsService.getResumeAnalysisList(currentUserId, pageRequest);

        return ResponseEntity.ok(response);
    }

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
