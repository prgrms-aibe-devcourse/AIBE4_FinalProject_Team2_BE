package com.aibe.team2.domain.interview.repository;

import com.aibe.team2.domain.interview.enums.InterviewMode;
import com.aibe.team2.domain.interview.enums.InterviewType;
import com.aibe.team2.domain.mypage.dto.response.InterviewSessionListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InterviewSessionRepositoryCustom {
    Page<InterviewSessionListResponse> findInterviewSessionList(
            Long memberId,
            InterviewType type,
            String keyword,
            Pageable pageable
    );
}
