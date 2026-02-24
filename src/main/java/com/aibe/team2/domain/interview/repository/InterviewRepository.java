package com.aibe.team2.domain.interview.repository;

import com.aibe.team2.domain.interview.entity.InterviewSession;
import com.aibe.team2.domain.interview.entity.InterviewSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewRepository extends JpaRepository<InterviewSession, Long> {
    // [추가] 완료된 면접 기록을 최신순으로 가져오기 (마이페이지 통계용)
    List<InterviewSession> findAllByMemberIdAndStatusOrderByCreatedAtDesc(Long memberId, InterviewSessionStatus status);
}