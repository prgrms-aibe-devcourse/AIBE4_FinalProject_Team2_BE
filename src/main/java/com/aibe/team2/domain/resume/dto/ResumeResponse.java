package com.aibe.team2.domain.resume.dto;

import com.aibe.team2.domain.resume.entity.Resume;

public record ResumeResponse(
        Long id,
        String title,
        String content,
        Boolean isAnalyzed
) {
    public static ResumeResponse from(Resume resume) {
        return new ResumeResponse(
                resume.getId(),
                resume.getTitle(),
                resume.getContent(),
                resume.getIsAnalyzed()
        );
    }
}