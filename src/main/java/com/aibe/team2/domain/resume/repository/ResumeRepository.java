package com.aibe.team2.domain.resume.repository;

import com.aibe.team2.domain.resume.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {

    // 특정 사용자의 모든 이력서 조회 (마이페이지/이력서 관리 등에서 사용)
    List<Resume> findAllByMemberId(Long memberId);

    // Id와 MemberId를 함께 검증하여 조회 (보안상 본인 이력서만 접근 가능하게 할 때 사용)
    Optional<Resume> findByIdAndMemberId(Long id, Long memberId);
}