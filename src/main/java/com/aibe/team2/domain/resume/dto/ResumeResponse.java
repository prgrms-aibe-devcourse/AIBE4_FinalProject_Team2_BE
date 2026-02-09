package com.aibe.team2.domain.resume.dto;

import com.aibe.team2.domain.resume.entity.Resume;

/**
 * 아래의 코드는 전부 예시입니다. 추후 기능이나 필요에 맞게 자유롭게 수정하면 됩니다.
 */
public record ResumeResponse(Long id, String title, String feedback) {
    public static ResumeResponse from(Resume resume) {
        return new ResumeResponse(
                resume.getId(),
                resume.getTitle(),
                resume.getFeedback()
        );
    }
}