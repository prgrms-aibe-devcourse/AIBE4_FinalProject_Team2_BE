package com.aibe.team2.domain.interview.dto;

import com.aibe.team2.domain.interview.enums.InterviewMode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InterviewRequestDto {
    private String message;
    private String modelVariant;
    private InterviewMode interviewMode;
    private String resumeContent; // [FR-INT-06] 프롬프트에 주입할 이력서 원문 데이터 추가
}