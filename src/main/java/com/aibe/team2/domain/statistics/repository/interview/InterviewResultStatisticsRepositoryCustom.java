package com.aibe.team2.domain.statistics.repository.interview;

import com.aibe.team2.domain.statistics.entity.InterviewResultStatistics;

import java.util.List;

public interface InterviewResultStatisticsRepositoryCustom {

    // 특정 회원의 통계 결화를 조회하며, 면접 타입(TEXT/VOICE)에 따라 동적 필터링을 수행
    List<InterviewResultStatistics> findStatisticsByCondition(Long memberId, String sessionType);
}
