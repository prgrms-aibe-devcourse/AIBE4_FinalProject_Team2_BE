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

    // [FR-INT-06, 07] 프롬프트에 동적 주입할 컨텍스트 데이터 필드 추가
    private String resumeContent;
    private String jobDescription;
}