package com.aibe.team2.domain.statistics.controller;

import com.aibe.team2.domain.auth.dto.CustomUserDetails;
import com.aibe.team2.domain.statistics.dto.DailyStatisticsResponse;
import com.aibe.team2.domain.statistics.service.DailyStatisticsService;
import com.aibe.team2.global.common.response.ApiResponse;
import com.aibe.team2.global.redis.ratelimit.RateLimit;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mypage/statistics")
@RequiredArgsConstructor
public class DailyStatisticsController {

    private final DailyStatisticsService dailyStatisticsService;

    private Long getMemberIdWithFallback(CustomUserDetails userDetails) {
        if(userDetails == null || userDetails.getMember() == null) {
            // TODO : 현재 개발 및 테스트 환경을 위한 Fallback ID 반환
            return 1L;
        }
        return userDetails.getMember().getMemberId();
    }

    // [FR-STA-02] 일 단위 통계 집계 API
    // GET /api/v1/mypage/statistics?targetDate=2026-02-26
    @RateLimit
    @GetMapping
    public ResponseEntity<ApiResponse<DailyStatisticsResponse>> getDailyStatistics(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(value = "targetDate", required = false) String targetDate
    ){
        Long memberId = Long.parseLong(userDetails.getUsername());
        DailyStatisticsResponse result = dailyStatisticsService.getDailyStatistics(memberId, targetDate);

        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
