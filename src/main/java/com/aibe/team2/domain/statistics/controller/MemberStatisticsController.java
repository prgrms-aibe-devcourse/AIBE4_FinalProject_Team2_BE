package com.aibe.team2.domain.statistics.controller;

import com.aibe.team2.domain.auth.dto.CustomUserDetails;
import com.aibe.team2.domain.statistics.dto.usage.MonthlyUsageResponse;
import com.aibe.team2.domain.statistics.service.MemberStatisticsService;
import com.aibe.team2.global.redis.ratelimit.RateLimit;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    // Fallback 로직: 인증 정보가 없을 시 임시 ID 반환
    private Long getMemberIdWithFallback(CustomUserDetails userDetails) {
        if(userDetails == null || userDetails.getMember() == null) {
            // TODO : 현재 개발 및 테스트 환경을 위한 Fallback ID 반환
            return 1L;
        }
        return userDetails.getMember().getMemberId();
    }

    // [FR-STA-01] 월별 사용량 조회
    // GET /api/v1/mypage/ai-usage
    @RateLimit
    @GetMapping("/ai-usage")
    public ResponseEntity<MonthlyUsageResponse> getMonthlyAiUsage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(name = "year", required = false) Integer year
    ) {
        Long memberId = getMemberIdWithFallback(userDetails);

        int targetYear = (year != null ? year : LocalDate.now().getYear());
        MonthlyUsageResponse response = memberStatisticsService.getMonthlyUsageStatistics(memberId, targetYear);

        return ResponseEntity.ok(response);
    }
}
