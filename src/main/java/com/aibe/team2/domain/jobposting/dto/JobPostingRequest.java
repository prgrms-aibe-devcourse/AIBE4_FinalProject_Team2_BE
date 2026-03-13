package com.aibe.team2.domain.jobposting.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record JobPostingRequest(
        String companyName,
        String jobTitle,
        String postingUrl,
        String jobDescription,
        String mainTasks,
        String qualifications,
        String preferred,
        String benefits,

        @Schema(description = "예상 면접 질문 (JSON Array String 포맷)", example = "[\"질문1\", \"질문2\"]")
        String expectedQuestions
) {}