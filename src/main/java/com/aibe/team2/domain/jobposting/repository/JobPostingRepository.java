package com.aibe.team2.domain.jobposting.repository;

import com.aibe.team2.domain.jobposting.entity.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {

    // 특정 유저의 공고를 등록일 기준 최신순으로 조회
    List<JobPosting> findAllByMemberIdOrderByCreatedAtDesc(Long memberId);

    // pgvector: 내 이력서의 벡터 값을 받아, 가장 유사한 채용 공고 Top 5를 검색 (거리 오름차순)
    @Query(value = "SELECT * FROM job_posting ORDER BY embedding <=> cast(:resumeVector as vector) LIMIT 5", nativeQuery = true)
    List<JobPosting> findTop5SimilarJobPostings(@Param("resumeVector") String resumeVectorString);
    // [FR-INT-07] IDOR 방어: Id와 MemberId를 함께 검증하여 공고 조회 / 작성자 : 최원준
    Optional<JobPosting> findByIdAndMemberId(Long id, Long memberId);
}