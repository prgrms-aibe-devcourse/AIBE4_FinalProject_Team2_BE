package com.aibe.team2.domain.jobposting.repository;

import com.aibe.team2.domain.jobposting.entity.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
    // 사용자의 공고 목록 조회 (최신순)
    List<JobPosting> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);
}