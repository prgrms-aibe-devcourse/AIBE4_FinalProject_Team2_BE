package com.aibe.team2.domain.statistics.dto.usage;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MonthlyUsageResponse {

    private Long memberId; // 사용자 ID
    private int year; // 조회한 연도
    private Long totalAmount; // 1년 총 사용량 합계

    private List<MonthlyUsageStatDto> monthlyStats;

    public static MonthlyUsageResponse of(Long memberId, int year, List<MonthlyUsageStatDto> stats) {
        long totalSum = stats.stream()
                .mapToLong(MonthlyUsageStatDto::getAmount)
                .sum();

        return MonthlyUsageResponse.builder()
                .memberId(memberId)
                .year(year)
                .totalAmount(totalSum)
                .monthlyStats(stats)
                .build();
    }
}
