package com.aibe.team2.domain.resume.dto;

import jakarta.validation.constraints.NotBlank;

public record AnalysisMatchRequest(
        @NotBlank(message = "채용 공고 내용은 필수입니다.")
        String jobDescriptionText
) {
}