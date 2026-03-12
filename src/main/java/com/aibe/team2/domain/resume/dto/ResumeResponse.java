package com.aibe.team2.domain.resume.dto;

import com.aibe.team2.domain.resume.entity.Resume;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ResumeResponse(
        Long id,
        Long memberId,
        String title,
        String s3FileUrl,
        String content,
        Boolean isAnalyzed,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ResumeResponse from(Resume resume) {
        return ResumeResponse.builder()
                .id(resume.getId())
                .memberId(resume.getMemberId())
                .title(resume.getTitle())
                .s3FileUrl(resume.getS3FileUrl())
                .content(resume.getContent())
                .isAnalyzed(resume.getIsAnalyzed())
                .createdAt(resume.getCreatedAt())
                .updatedAt(resume.getUpdatedAt())
                .build();
    }
}