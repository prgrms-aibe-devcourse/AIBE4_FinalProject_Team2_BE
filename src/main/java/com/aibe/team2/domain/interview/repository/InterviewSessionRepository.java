package com.aibe.team2.domain.interview.repository;

import com.aibe.team2.domain.interview.entity.InterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long>, InterviewSessionRepositoryCustom {

    // 오늘 하루(Start~End)동안 생성된 면접 세션 개수 조회
    // 🚀 [FR-INT-09] ABORTED 상태인 세션은 통계에서 제외 (Service 계층 코드 수정 방지)
    @Query("SELECT count(i) FROM InterviewSession i WHERE i.memberId = :memberId AND i.status != 'ABORTED' AND i.createdAt BETWEEN :start AND :end")
    long countByMemberIdAndCreatedAtBetween(@Param("memberId") Long memberId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}