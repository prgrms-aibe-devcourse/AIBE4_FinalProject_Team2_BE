package com.aibe.team2.domain.resume.repository;

import com.aibe.team2.domain.resume.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 아래의 코드는 전부 예시입니다. 추후 기능이나 필요에 맞게 자유롭게 수정하면 됩니다.
 */
@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {

}