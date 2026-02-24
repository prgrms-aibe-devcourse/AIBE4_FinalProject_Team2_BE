package com.aibe.team2.domain.jobposting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record JobPostingRequest(
        Long userId,
        String companyName,
        @NotBlank(message = "직무명은 필수입니다.") String jobTitle,
        String postingUrl,
        String jobDescription,

        @Schema(description = "요구 역량 리스트", example = "[\"Java\", \"Spring Boot\"]")
        List<String> requiredSkills // List<String>으로 변경
) {}