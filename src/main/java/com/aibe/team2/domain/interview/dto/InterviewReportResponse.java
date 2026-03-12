package com.aibe.team2.domain.interview.dto;

import com.aibe.team2.domain.interview.entity.InterviewSession;
import com.aibe.team2.domain.interview.enums.InterviewMode;
import com.aibe.team2.domain.interview.enums.InterviewSessionStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class InterviewReportResponse {
    private Long sessionId;
    private InterviewMode interviewMode;
    private String interviewType;
    private InterviewSessionStatus status;
    private Integer finalScore;
    private LocalDateTime createdAt;

    // 연관된 이력서와 공고의 '제목'을 프론트엔드에 전달하기 위한 필드
    private String resumeTitle;
    private String jobTitle;

    public static InterviewReportResponse of(InterviewSession session, String resumeTitle, String jobTitle) {
        return InterviewReportResponse.builder()
                .sessionId(session.getId())
                .interviewMode(session.getInterviewMode())
                .interviewType(session.getInterviewType())
                .status(session.getStatus())
                .finalScore(session.getFinalScore())
                .createdAt(session.getCreatedAt())
                .resumeTitle(resumeTitle)
                .jobTitle(jobTitle)
                .build();
    }
}