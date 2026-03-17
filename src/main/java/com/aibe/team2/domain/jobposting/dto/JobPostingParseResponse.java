package com.aibe.team2.domain.jobposting.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record JobPostingParseResponse(
        @Schema(description = "기업명") String companyName,
        @Schema(description = "직무명") String jobTitle,
        @Schema(description = "공고 전체 내용") String jobDescription,
        @Schema(description = "주요 업무") String mainTasks,
        @Schema(description = "자격 요건") String qualifications,
        @Schema(description = "우대 사항") String preferred,
        @Schema(description = "복지 및 혜택") String benefits,
        @Schema(description = "요구 역량 리스트") List<String> requiredSkills,
        @Schema(description = "예상 면접 질문 리스트") List<String> expectedQuestions
) {}