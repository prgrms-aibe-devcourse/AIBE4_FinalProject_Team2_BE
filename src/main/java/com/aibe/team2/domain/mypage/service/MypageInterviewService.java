package com.aibe.team2.domain.mypage.service;

import com.aibe.team2.domain.interview.enums.InterviewMode;
import com.aibe.team2.domain.interview.enums.InterviewType;
import com.aibe.team2.domain.interview.repository.InterviewSessionRepository;
import com.aibe.team2.domain.mypage.dto.response.InterviewSessionListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MypageInterviewService {

    private final InterviewSessionRepository interviewSessionRepository;

    public Page<InterviewSessionListResponse> getInterviewSessionList(Long memberId, InterviewType type, String keyword, Pageable pageable) {

        return interviewSessionRepository.findInterviewSessionList(memberId, type, keyword, pageable);
    }
}
