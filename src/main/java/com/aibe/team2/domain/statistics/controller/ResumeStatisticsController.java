package com.aibe.team2.domain.statistics.controller;

import com.aibe.team2.domain.auth.dto.CustomUserDetails;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mypage/resumes")
public class ResumeStatisticsController {

    private final ResumeStatisticsService resumeStatisticsService;

    // Fallback 로직: 인증 정보가 없을 시 임시 ID 반환
    private Long getMemberIdWithFallback(CustomUserDetails userDetails) {
        if(userDetails == null || userDetails.getMember() == null) {
            // TODO : 현재 개발 및 테스트 환경을 위한 Fallback ID 반환
            return 1L;
        }
        return userDetails.getMember().getMemberId();
    }

    // [FR-REP-04] 자기소개서 첨삭 이력 조회
    @RateLimit
    @GetMapping("/analysis")
    public ResponseEntity<Page<ResumeAnalysisListResponse>> getResumeAnalysisList(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page, // URL 파라미터: 시작 페이지 (기본 0)
            @RequestParam(defaultValue = "10") int size // URL 파라미터: 페이지당 개수 (기본 10개)
    ) {
        Long memberId = getMemberIdWithFallback(userDetails);
        // 최신순으로 정렬하는 페이징 객체 생성
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ResumeAnalysisListResponse> response = resumeStatisticsService.getResumeAnalysisList(memberId, pageRequest);

        return ResponseEntity.ok(response);
    }

    // [FR-REP-04] 자기소개서 첨삭 이력 - 상세 조회(리포트)
    @GetMapping("/analysis/{analysisId}")
    public ResponseEntity<ResumeAnalysisResultResponse> getResumeAnalysisReport(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("analysisId") Long analysisId
    ){
        Long memberId = getMemberIdWithFallback(userDetails);
        ResumeAnalysisResultResponse response = resumeStatisticsService.getResumeAnalysisReport(analysisId, memberId);

        return ResponseEntity.ok(response);
    }
}
