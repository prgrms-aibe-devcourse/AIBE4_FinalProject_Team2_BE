package com.aibe.team2.domain.interview.repository;

import com.aibe.team2.domain.mypage.dto.InterviewSessionListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InterviewSessionRepositoryCustom {
    Page<InterviewSessionListResponse> findInterviewSessionList(Long memberId, Pageable pageable);
}
