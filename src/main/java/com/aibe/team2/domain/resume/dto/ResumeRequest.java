package com.aibe.team2.domain.resume.dto;

import jakarta.validation.constraints.NotBlank;

public record ResumeRequest(
        @NotBlank(message = "이력서 제목은 필수입니다.")
        String title,

        @NotBlank(message = "이력서 내용은 필수입니다.")
        String content
) {
}