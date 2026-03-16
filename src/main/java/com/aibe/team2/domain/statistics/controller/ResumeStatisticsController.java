package com.aibe.team2.domain.statistics.controller;

import com.aibe.team2.domain.resume.dto.ResumeStatsResponse;
import com.aibe.team2.domain.statistics.dto.resume.ResumeAnalysisListResponse;
import com.aibe.team2.domain.statistics.dto.resume.ResumeAnalysisResultResponse;
import com.aibe.team2.domain.statistics.service.ResumeStatisticsService;
import com.aibe.team2.global.common.annotation.LoginMemberId;
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
            @LoginMemberId Long memberId,
            @RequestParam(defaultValue = "0") int page, // URL 파라미터: 시작 페이지 (기본 0)
            @RequestParam(defaultValue = "10") int size // URL 파라미터: 페이지당 개수 (기본 10개)
    ) {
        // 최신순으로 정렬하는 페이징 객체 생성
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ResumeAnalysisListResponse> response = resumeStatisticsService.getResumeAnalysisList(memberId, pageRequest);

        return ResponseEntity.ok(response);
    }

    // [FR-REP-04] 자기소개서 첨삭 이력 - 상세 조회(리포트)
    @GetMapping("/analysis/{analysisId}")
    public ResponseEntity<ResumeAnalysisResultResponse> getResumeAnalysisReport(
            @LoginMemberId Long memberId,
            @PathVariable("analysisId") Long analysisId
    ){
        ResumeAnalysisResultResponse response = resumeStatisticsService.getResumeAnalysisReport(analysisId, memberId);

        return ResponseEntity.ok(response);
    }

    // 내 자기소개서 요약 통계 조회
    @GetMapping("/stats")
    public ResponseEntity<ResumeStatsResponse> getResumeStats(
            @LoginMemberId Long memberId
    ){
        log.info("Dashboard resume stats requested. memberId: {}", memberId);

        // 서비스 계층에 통계 데이터 요청
        ResumeStatsResponse response = resumeStatisticsService.getResumeStats(memberId);

        return ResponseEntity.ok(response);
    }
}
