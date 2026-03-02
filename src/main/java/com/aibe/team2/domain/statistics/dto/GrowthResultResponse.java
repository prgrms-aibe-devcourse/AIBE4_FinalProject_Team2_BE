package com.aibe.team2.domain.statistics.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class GrowthResultResponse {
    private String metricName;
    private BigDecimal previousValue;
    private BigDecimal currentValue;
    private BigDecimal difference;
    private BigDecimal growthRate;
    private String displayGrowthRate; // "신규 진입" 등의 예외 문자열 처리를 위해 추가
}
