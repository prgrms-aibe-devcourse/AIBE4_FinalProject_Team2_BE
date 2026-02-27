package com.aibe.team2.domain.statistics.controller;

import com.aibe.team2.domain.statistics.dto.GrowthResultResponse;
import com.aibe.team2.domain.statistics.service.GrowthStatisticsService;
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

    @GetMapping("/statistics/growth")
    public ResponseEntity<List<GrowthResultResponse>>getGrowthStatistics(
            // TODO : Spring Security 구현 후 수정
            // @AuthenticationPrincipal CustomUserDetails userDetails
    ) {

        // [변경 예정]
        // Long currentMemberId = userDetails.getMemberId();
        Long currentMemberId = 1L;
        List<GrowthResultResponse> response = growthStatisticsService.getGrowthStatistics(currentMemberId);

        return ResponseEntity.ok(response);
    }
}
