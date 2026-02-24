package com.aibe.team2.domain.statistics.controller;

import com.aibe.team2.domain.statistics.dto.usage.MonthlyUsageResponse;
import com.aibe.team2.domain.statistics.service.MemberStatisticsService;
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
    @GetMapping("/ai-usage")
    public ResponseEntity<MonthlyUsageResponse> getMonthlyAiUsage(
            // TODO : Spring Security 구현 후 주석 해제 및 임시 memberId 파라미터 삭제 필요
            // @AuthenticationPrincipal UserDetails userDetails,

            // [임시] 로그인한 사용자 정보 가져오기
            @RequestParam(name = "memberId", required = false, defaultValue = "1") Long memberId,
            @RequestParam(name = "year", required = false) Integer year
    ) {
        int targetYear = (year != null ? year : LocalDate.now().getYear());
        MonthlyUsageResponse response = memberStatisticsService.getMonthlyUsageStatistics(memberId, targetYear);

        return ResponseEntity.ok(response);
    }
}
