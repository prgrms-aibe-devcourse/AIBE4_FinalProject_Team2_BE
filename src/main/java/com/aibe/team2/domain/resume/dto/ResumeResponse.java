package com.aibe.team2.domain.resume.dto;

import com.aibe.team2.domain.resume.entity.Resume;

public record ResumeResponse(
        Long resumeId,
        Long memberId,
        String title,
        String content,
        Boolean isAnalyzed
) {
    public static ResumeResponse from(Resume resume) {
        return new ResumeResponse(
                resume.getId(),
                resume.getMemberId(),
                resume.getTitle(),
                resume.getContent(),
                resume.getIsAnalyzed()
        );
    }
}