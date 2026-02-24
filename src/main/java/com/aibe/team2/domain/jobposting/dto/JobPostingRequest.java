package com.aibe.team2.domain.jobposting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record JobPostingRequest(
        // Long memberId,  <-- 보안을 위해 삭제하세요!

        @Schema(description = "기업명", example = "원티드랩")
        String companyName,

        @NotBlank(message = "직무명은 필수입니다.")
        @Schema(description = "직무명", example = "백엔드 개발자")
        String jobTitle,

        @Schema(description = "채용 공고 URL (입력 시 내용 자동 크롤링)", example = "https://www.wanted.co.kr/wd/123456")
        String postingUrl,

        @Schema(description = "채용 공고 본문 (URL 입력 시 자동 채움 가능)", example = "주요 업무: Java 백엔드 개발...")
        String jobDescription,

        @Schema(description = "요구 역량 리스트", example = "[\"Java\", \"Spring Boot\"]")
        List<String> requiredSkills
) {}