package com.aibe.team2.domain.jobposting.repository;

import com.aibe.team2.domain.jobposting.entity.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {

    // 특정 유저의 공고를 등록일 기준 최신순으로 조회
    List<JobPosting> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);

    // [FR-INT-07] IDOR 방어: Id와 MemberId를 함께 검증하여 공고 조회 / 작성자 : 최원준
    Optional<JobPosting> findByIdAndMemberId(Long id, Long memberId);
}