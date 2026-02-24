package com.aibe.team2.domain.jobposting.dto;

import com.aibe.team2.domain.jobposting.entity.JobPosting;
import com.aibe.team2.domain.jobposting.entity.JobSkill;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public record JobPostingResponse(
        Long id,
        Long userId,
        String companyName,
        String jobTitle,
        String postingUrl,
        String jobDescription,

        @Schema(description = "요구 역량 (JSON Array String)")
        List<String> requiredSkills,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static JobPostingResponse from(JobPosting jobPosting) {
        return new JobPostingResponse(
                jobPosting.getId(),
                jobPosting.getUserId(),
                jobPosting.getCompanyName(),
                jobPosting.getJobTitle(),
                jobPosting.getPostingUrl(),
                jobPosting.getJobDescription(),
                jobPosting.getJobSkills().stream()
                        .map(JobSkill::getSkillName)
                        .toList(),
                jobPosting.getCreatedAt(),
                jobPosting.getUpdatedAt()
        );
    }
}