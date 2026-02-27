package com.aibe.team2.domain.statistics.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class DailyStatisticsResponse {
    private String targetDate; // 집계 기준 일자
    private int resumeReviewCount; // 자기소개서 첨삭 건수
    private int completedInterviewCount; // 모의 면접 완료 건수
    private double averageInterviewScore; // 모의 면접 평균 점수(추가 계산 필요 시 사용)
    private int totalAiUsageMinutes; // 총 AI 사용 시간

    public static DailyStatisticsResponse of(LocalDate date, int resumeCount, int interviewCount){
        return DailyStatisticsResponse.builder()
                .targetDate(date.toString())
                .resumeReviewCount(resumeCount)
                .completedInterviewCount(interviewCount)
                .averageInterviewScore(0.0)
                .totalAiUsageMinutes(0)
                .build();
    }
}