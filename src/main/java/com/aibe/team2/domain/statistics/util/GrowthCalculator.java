package com.aibe.team2.domain.statistics.util;

import com.aibe.team2.domain.statistics.dto.GrowthResultResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class GrowthCalculator {

    // 인스턴스화 방지
    private GrowthCalculator() {
        throw new IllegalStateException("Utility class");
    }

    // 성장률 계산 및 DTO 반환 메서드
    public static GrowthResultResponse calculateGrowth(String metricName, BigDecimal previous, BigDecimal current) {

        // null 방어
        if(previous == null || current == null) {
            throw new IllegalArgumentException("previous/current는 null일 수 없습니다.");
        }

        // 소수점 둘째 자리 반올림
        BigDecimal roundedPrevious = previous.setScale(2, RoundingMode.HALF_UP);
        BigDecimal roundedCurrent = current.setScale(2, RoundingMode.HALF_UP);

        // 변화량 계산
        BigDecimal difference = current.subtract(previous);

        // 0으로 나누기 방지 및 신규 사용자 처리 로직
        if(previous.compareTo(BigDecimal.ZERO) == 0) {
            return GrowthResultResponse.builder()
                    .metricName(metricName)
                    .previousValue(previous)
                    .currentValue(current)
                    .difference(difference)
                    .growthRate(null) // 이전 값이 0이면 성장률 수치로 정의할 수 없음
                    .displayGrowthRate("신규 진입")
                    .build();
        }

        // 성장률 계산 : (Difference / Previous) * 100
        // 소수점 셋째 자리에서 반올림하여 둘째 자리까지 표현
        BigDecimal growthRate = difference.divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);

        return GrowthResultResponse.builder()
                .metricName(metricName)
                .previousValue(roundedPrevious)
                .currentValue(roundedCurrent)
                .difference(difference)
                .growthRate(growthRate)
                .displayGrowthRate(growthRate.toString() + "%")
                .build();
    }
}
