package com.aibe.team2.domain.interview.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InterviewStartRequest {
    private Long memberId;
    private Long resumeId;
    private Long jobPostingId;
    private String interviewMode;
    private String interviewType;
    private String aiProvider;
    private String modelVariant;
    private String personaType;
}