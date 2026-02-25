package com.aibe.team2.domain.resume.dto;

import jakarta.validation.constraints.NotBlank;

public record ResumeRequest(
        Long memberId,

        @NotBlank(message = "제목은 필수입니다.")
        String title,

        String content
) {
}