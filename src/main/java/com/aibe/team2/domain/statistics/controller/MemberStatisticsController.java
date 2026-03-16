package com.aibe.team2.domain.statistics.controller;

import com.aibe.team2.domain.statistics.dto.usage.MonthlyUsageResponse;
import com.aibe.team2.domain.statistics.service.MemberStatisticsService;
import com.aibe.team2.global.common.annotation.LoginMemberId;
import com.aibe.team2.global.redis.ratelimit.RateLimit;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/mypage")
@RequiredArgsConstructor
public class MemberStatisticsController {

    private final MemberStatisticsService memberStatisticsService;

    // [FR-STA-01] 월별 사용량 조회
    // GET /api/v1/mypage/ai-usage
    @RateLimit
    @GetMapping("/ai-usage")
    public ResponseEntity<MonthlyUsageResponse> getMonthlyAiUsage(
            @LoginMemberId Long memberId,
            @RequestParam(name = "year", required = false) Integer year
    ) {
        int targetYear = (year != null ? year : LocalDate.now().getYear());
        MonthlyUsageResponse response = memberStatisticsService.getMonthlyUsageStatistics(memberId, targetYear);

        return ResponseEntity.ok(response);
    }
}
