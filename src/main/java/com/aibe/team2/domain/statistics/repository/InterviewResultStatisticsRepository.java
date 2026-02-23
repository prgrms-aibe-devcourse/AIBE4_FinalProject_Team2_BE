package com.aibe.team2.domain.statistics.repository;

import com.aibe.team2.domain.statistics.entity.InterviewResultStatistics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InterviewResultStatisticsRepository extends JpaRepository<InterviewResultStatistics, Long> {

    // 특정 면접 세션 ID를 통해 통계 데이터 조회
    // @param sessionId 면접 세션 고유 ID
    // @return 해당 세션의 통계 데이터(Optional)

    Optional <InterviewResultStatistics> findByInterviewSessionId(Long sessionId);
}
