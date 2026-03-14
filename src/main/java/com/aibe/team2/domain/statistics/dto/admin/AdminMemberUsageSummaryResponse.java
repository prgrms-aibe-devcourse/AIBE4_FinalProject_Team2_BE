package com.aibe.team2.domain.statistics.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminMemberUsageSummaryResponse {

    private Long memberId;
    private String email;
    private Integer creditBalance;

    private Long totalLogCount;

    private Long resumeUsageCount;
    private Long interviewUsageCount;

    private Long serviceTokenUsage;   // 실제 서비스 사용 토큰
    private Long adminCreditDelta;    // 관리자 크레딧 변동량
}