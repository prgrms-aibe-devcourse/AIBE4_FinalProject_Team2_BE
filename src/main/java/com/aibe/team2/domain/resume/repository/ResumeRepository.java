package com.aibe.team2.domain.resume.repository;

import com.aibe.team2.domain.resume.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {

    List<Resume> findAllByMemberId(Long memberId);

    Optional<Resume> findByIdAndMemberId(Long id, Long memberId);

    // =========================================================
    // 대시보드 통계용
    // =========================================================
    // 1. 내 저장된 이력서 총 개수
    long countByMemberId(Long memberId);

    // 2. AI 분석 완료된 자소서 개수 (isAnalyzed 가 true인 것)
    long countByMemberIdAndIsAnalyzedTrue(Long memberId);
}