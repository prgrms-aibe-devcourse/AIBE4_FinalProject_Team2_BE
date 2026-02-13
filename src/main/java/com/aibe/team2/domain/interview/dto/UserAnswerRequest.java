package com.aibe.team2.domain.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserAnswerRequest {
    private String content; // 사용자의 텍스트 답변 내용
}