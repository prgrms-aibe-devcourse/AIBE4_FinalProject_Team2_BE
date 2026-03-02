package com.aibe.team2.domain.statistics.controller;

import com.aibe.team2.domain.statistics.dto.DailyStatisticsResponse;
import com.aibe.team2.domain.statistics.service.DailyStatisticsService;
import com.aibe.team2.global.common.response.ApiResponse;
import com.aibe.team2.global.redis.ratelimit.RateLimit;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/mypage/statistics")
@RequiredArgsConstructor
public class DailyStatisticsController {

    private final DailyStatisticsService dailyStatisticsService;

    // [FR-STA-02] 일 단위 통계 집계 API
    // GET /api/v1/mypage/statistics?targetDate=2026-02-26
    @RateLimit
    @GetMapping
    public ResponseEntity<ApiResponse<DailyStatisticsResponse>> getDailyStatistics(
            // TODO : Spring Security 적용 후 주석 해제 및 Mock-Member-Id 제거
            // @AuthenticationPrincipal UserDetails userDetails,

            // [임시] Security 구현 전 테스트를 위해 Header로 memberId를 받음
            @RequestHeader(value = "Mock-Member-Id", defaultValue = "1") Long memberId,
            @RequestParam(value = "targetDate", required = false) String targetDate
    ){
        // [임시] Security 구현 후 복구
        // Long memberId = Long.parseLong(userDetails.getUsername());
        DailyStatisticsResponse result = dailyStatisticsService.getDailyStatistics(memberId, targetDate);

        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
