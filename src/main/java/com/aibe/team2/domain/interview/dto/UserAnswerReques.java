package com.aibe.team2.domain.interview.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserAnswerRequest {
    private String content; // 사용자의 답변 내용 (텍스트/음성 경로)
}