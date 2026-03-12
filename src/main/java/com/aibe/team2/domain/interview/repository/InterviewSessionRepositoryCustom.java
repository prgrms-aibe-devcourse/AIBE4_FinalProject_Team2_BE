package com.aibe.team2.domain.interview.repository;

import com.aibe.team2.domain.interview.enums.InterviewType;
import com.aibe.team2.domain.mypage.dto.response.InterviewSessionListResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InterviewSessionRepositoryCustom {
    List<InterviewSessionListResponse> findInterviewSessionList(
            Long memberId,
            InterviewType type,
            String keyword,
            Pageable pageable
    );
}
