package com.aibe.team2.domain.statistics.controller;

import com.aibe.team2.domain.auth.dto.CustomUserDetails;
import com.aibe.team2.domain.statistics.dto.GrowthResultResponse;
import com.aibe.team2.domain.statistics.service.GrowthStatisticsService;
import com.aibe.team2.global.redis.ratelimit.RateLimit;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mypage")
@RequiredArgsConstructor
public class GrowthStatisticsController {

    private final GrowthStatisticsService growthStatisticsService;

    private Long getMemberIdWithFallback(CustomUserDetails userDetails) {
        if(userDetails == null || userDetails.getMember() == null) {
            // TODO : 현재 개발 및 테스트 환경을 위한 Fallback ID 반환
            return 1L;
        }
        return userDetails.getMember().getMemberId();
    }

    @RateLimit
    @GetMapping("/statistics/growth")
    public ResponseEntity<List<GrowthResultResponse>>getGrowthStatistics(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        // [변경 예정]
        Long currentMemberId = getMemberIdWithFallback(userDetails);
        List<GrowthResultResponse> response = growthStatisticsService.getGrowthStatistics(currentMemberId);

        return ResponseEntity.ok(response);
    }
}
