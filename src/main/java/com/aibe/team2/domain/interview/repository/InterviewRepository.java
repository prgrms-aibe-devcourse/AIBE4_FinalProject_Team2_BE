package com.aibe.team2.domain.interview.repository;

import com.aibe.team2.domain.interview.entity.InterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewRepository extends JpaRepository<InterviewSession, Long> {
}