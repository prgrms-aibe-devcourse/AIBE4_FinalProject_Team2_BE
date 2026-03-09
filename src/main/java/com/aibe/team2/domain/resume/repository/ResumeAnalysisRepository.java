package com.aibe.team2.domain.resume.repository;

import com.aibe.team2.domain.resume.entity.AnalyzedReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ResumeAnalysisRepository extends JpaRepository<AnalyzedReport, Long> {

    // 특정 자기소개서의 최신 분석 결과 조회 (내림차순 정렬 후 첫 번째 데이터) // 마이페이지용
    Optional<AnalyzedReport> findTopByResumeIdOrderByCreatedAtDesc(Long resumeId);

    // 오늘 하루(Start~End)동안 생성된 자기소개서 개수 조회
    @Query("SELECT COUNT(r) FROM AnalyzedReport r WHERE r.resume.memberId = :memberId AND r.createdAt BETWEEN :start AND :end")
    long countByMemberIdAndCreatedAtBetween(
            @Param("memberId") Long memberId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query(value = "SELECT r FROM AnalyzedReport r " +
            "JOIN FETCH r.resume res " +
            "JOIN FETCH r.jobPosting jp " +
            "WHERE res.memberId = :memberId",
            countQuery = "SELECT count(r) FROM AnalyzedReport r WHERE r.resume.memberId = :memberId")
    Page<AnalyzedReport> findByMemberIdWithDetails(@Param("memberId") Long memberId, Pageable pageable);
}