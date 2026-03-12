package com.aibe.team2.domain.resume.dto;

import jakarta.validation.constraints.NotNull;

public record AnalysisMatchRequest(
        @NotNull(message = "채용 공고 ID는 필수입니다.")
        Long jobPostingId
) {
}