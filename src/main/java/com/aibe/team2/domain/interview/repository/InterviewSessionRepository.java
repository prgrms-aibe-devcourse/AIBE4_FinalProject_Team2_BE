package com.aibe.team2.domain.interview.repository;

import com.aibe.team2.domain.interview.entity.InterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {

    // 오늘 하루(Start~End)동안 생성된 면접 세션 개수 조회
    long countByMemberIdAndCreatedAtBetween(Long memberId, LocalDateTime start, LocalDateTime end);
}
