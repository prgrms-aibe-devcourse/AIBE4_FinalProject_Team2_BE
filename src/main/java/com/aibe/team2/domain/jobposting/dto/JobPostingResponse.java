package com.aibe.team2.domain.jobposting.dto;

import com.aibe.team2.domain.jobposting.entity.JobPosting;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record JobPostingResponse(
        Long jobPostingId,
        Long memberId,
        String companyName,
        String jobTitle,
        String postingUrl,
        String jobDescription,
        String mainTasks,
        String qualifications,
        String preferred,
        String benefits,

        @Schema(description = "예상 면접 질문 (JSON String)")
        String expectedQuestions,

        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static JobPostingResponse from(JobPosting jobPosting) {
        return new JobPostingResponse(
                jobPosting.getId(),
                jobPosting.getMemberId(),
                jobPosting.getCompanyName(),
                jobPosting.getJobTitle(),
                jobPosting.getPostingUrl(),
                jobPosting.getJobDescription(),
                jobPosting.getMainTasks(),
                jobPosting.getQualifications(),
                jobPosting.getPreferred(),
                jobPosting.getBenefits(),
                jobPosting.getExpectedQuestions(),
                jobPosting.getCreatedAt(),
                jobPosting.getUpdatedAt()
        );
    }
}