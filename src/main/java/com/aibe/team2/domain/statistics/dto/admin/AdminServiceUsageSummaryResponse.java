package com.aibe.team2.domain.statistics.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminServiceUsageSummaryResponse {

    // 사용 건수
    private Long resumeUsageCount;
    private Long interviewUsageCount;
    private Long adminOperationCount;

    // 합계
    private Long totalServiceUsageCount;   // RESUME + INTERVIEW
    private Long totalOverallLogCount;     // RESUME + INTERVIEW + ADMIN

    // 양 분리
    private Long serviceTokenUsage;        // 실제 서비스(RESUME, INTERVIEW) tokenUsage 합
    private Long adminCreditDelta;         // 관리자 지급/차감 합계
}