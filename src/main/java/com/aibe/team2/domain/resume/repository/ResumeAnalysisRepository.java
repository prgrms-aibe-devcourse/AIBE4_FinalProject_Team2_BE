package com.aibe.team2.domain.resume.repository;

import com.aibe.team2.domain.resume.entity.ResumeAnalysisReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysisReport, Long> {

    // 특정 이력서의 최신 분석 결과 조회 (내림차순 정렬 후 첫 번째 데이터)
    Optional<ResumeAnalysisReport> findTopByResumeIdOrderByCreatedAtDesc(Long resumeId);
}