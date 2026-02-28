package com.aibe.team2.domain.mypage.dto;

import com.aibe.team2.domain.interview.entity.InterviewSessionStatus;
import java.time.LocalDateTime;

public record InterviewSessionListResponse(
        Long sessionId,
        String resumeTitle,     // QueryDSL이 가져올 자소서 제목
        String companyName,     // QueryDSL이 가져올 회사명
        String jobTitle,        // QueryDSL이 가져올 직무명
        String interviewMode,
        String interviewType,
        InterviewSessionStatus status,
        Integer finalScore,
        LocalDateTime createdAt
) {
    public InterviewSessionListResponse {
        resumeTitle = (resumeTitle != null) ? resumeTitle : "선택된 자소서 없음";
        companyName = (companyName != null) ? companyName : "자유 면접";
        jobTitle = (jobTitle != null) ? jobTitle : "-";
    }
}