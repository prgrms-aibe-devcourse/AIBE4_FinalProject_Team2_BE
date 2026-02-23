package com.aibe.team2.domain.statistics.repository;

import com.aibe.team2.domain.statistics.entity.InterviewRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewRecordRepository extends JpaRepository<InterviewRecord, Long> {

    // 특정 면접 세션의 모든 대화 기록을 대화 순서에 따라 정렬하여 조회
    // @param sessionId 면접 세션 고유 ID
    // @return 정렬된 대화 기록 리스트

    List<InterviewRecord> findAllByInterviewSessionIdOrderByTurnSequenceAsc(Long interviewSessionId);
}
