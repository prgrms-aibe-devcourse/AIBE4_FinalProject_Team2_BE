package com.aibe.team2.domain.statistics.controller;

import com.aibe.team2.domain.statistics.dto.GrowthResultResponse;
import com.aibe.team2.domain.statistics.service.GrowthStatisticsService;
import com.aibe.team2.global.common.annotation.LoginMemberId;
import com.aibe.team2.global.redis.ratelimit.RateLimit;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mypage")
@RequiredArgsConstructor
public class GrowthStatisticsController {

    private final GrowthStatisticsService growthStatisticsService;

    @RateLimit
    @GetMapping("/statistics/growth")
    public ResponseEntity<List<GrowthResultResponse>>getGrowthStatistics(
            @LoginMemberId Long memberId
    ) {
        List<GrowthResultResponse> response = growthStatisticsService.getGrowthStatistics(memberId);

        return ResponseEntity.ok(response);
    }
}
