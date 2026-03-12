package com.aibe.team2.domain.resume.repository;

import com.aibe.team2.domain.resume.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {

    List<Resume> findAllByMemberId(Long memberId);

    Optional<Resume> findByIdAndMemberId(Long id, Long memberId);

    long countByMemberIdAndCreatedAtBetween(Long memberId, LocalDateTime createdAtAfter, LocalDateTime createdAtBefore);

    // 채용공고의 벡터를 받아, 해당 직무에 가장 적합한 이력서 Top 5를 검색
    @Query(value = "SELECT * FROM resume ORDER BY embedding <=> cast(:jdVector as vector) LIMIT 5", nativeQuery = true)
    List<Resume> findTop5SimilarResumes(@Param("jdVector") String jdVectorString);
}