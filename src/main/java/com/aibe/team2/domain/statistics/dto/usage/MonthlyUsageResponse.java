package com.aibe.team2.domain.statistics.dto.usage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyUsageResponse {

    private Long memberId; // 사용자 ID
    private int year; // 조회한 연도
    private Long totalAmount; // 1년 총 사용량 합계

    private List<MonthlyUsageStatResponse> monthlyStats;

    public static MonthlyUsageResponse of(Long memberId, int year, List<MonthlyUsageStatResponse> stats) {
        long totalSum = stats.stream()
                .mapToLong(MonthlyUsageStatResponse::getAmount)
                .sum();

        return MonthlyUsageResponse.builder()
                .memberId(memberId)
                .year(year)
                .totalAmount(totalSum)
                .monthlyStats(stats)
                .build();
    }
}
