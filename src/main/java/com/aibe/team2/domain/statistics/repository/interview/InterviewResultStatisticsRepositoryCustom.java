package com.aibe.team2.domain.statistics.repository.interview;

import com.aibe.team2.domain.statistics.entity.InterviewResultStatistics;
import com.querydsl.core.Tuple;

import java.time.LocalDateTime;
import java.util.List;

public interface InterviewResultStatisticsRepositoryCustom {

    // 1. 특정 회원의 통계 결과를 조회하며, 면접 타입(TEXT/VOICE)에 따라 동적 필터링을 수행
    List<InterviewResultStatistics> findStatisticsByCondition(Long memberId, String sessionType);

    // 2. [추가된 튜플 메서드] 특정 기간 동안의 6개 지표 평균 계산
    Tuple findAverageMetricsTupleByMemberIdAndCreatedAtBetween(Long memberId, LocalDateTime start, LocalDateTime end);
}