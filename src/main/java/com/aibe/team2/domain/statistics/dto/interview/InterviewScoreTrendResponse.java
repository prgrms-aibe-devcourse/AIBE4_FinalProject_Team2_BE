package com.aibe.team2.domain.statistics.dto.interview;

import java.time.LocalDateTime;
import java.util.List;

public record InterviewScoreTrendResponse(
        Double averageScore, // 전체 평균 점수
        Integer latestScore, // 가장 최근 점수
        Double growthRate,   // 직전 대비 성장률 (+5.0, -2.0 등)
        List<ScoreHistory> trendList // 그래프용 시계열 데이터
) {
    public record ScoreHistory(
            Long sessionId,
            Integer score,
            LocalDateTime completedAt
    ) {}
}