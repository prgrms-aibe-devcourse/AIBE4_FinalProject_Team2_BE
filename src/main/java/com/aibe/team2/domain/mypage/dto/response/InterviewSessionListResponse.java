package com.aibe.team2.domain.mypage.dto.response;

import com.aibe.team2.domain.interview.enums.InterviewMode;
import com.aibe.team2.domain.interview.enums.InterviewSessionStatus;
import java.time.LocalDateTime;

public record InterviewSessionListResponse(
        Long sessionId,
        String resumeTitle,     // QueryDSL이 가져올 자소서 제목
        String companyName,     // QueryDSL이 가져올 회사명
        String jobTitle,        // QueryDSL이 가져올 직무명
       InterviewMode interviewMode,
        String interviewType,
        InterviewSessionStatus status,
        Integer finalScore,
        LocalDateTime createdAt
) {
    // 상수 추출
    private static final String NO_RESUME_SELECTED = "선택된 자기소개서 없음";
    private static final String FREESTYLE_INTERVIEW = "자유 면접";
    private static final String NO_JOB_TITLE = "-";
    public InterviewSessionListResponse {
        resumeTitle = (resumeTitle != null) ? resumeTitle : NO_RESUME_SELECTED;
        companyName = (companyName != null) ? companyName : FREESTYLE_INTERVIEW;
        jobTitle = (jobTitle != null) ? jobTitle : NO_JOB_TITLE;
    }
}