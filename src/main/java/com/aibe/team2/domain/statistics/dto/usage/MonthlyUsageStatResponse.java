package com.aibe.team2.domain.statistics.dto.usage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyUsageStatResponse {
    private int month; // 월(1~12)
    private String serviceType; // 서비스 타입(RESUME, INTERVIEW 등)
    private Long count; // 사용 횟수
    private Long amount; // 차감된 총 크레딧/토큰 양
}
