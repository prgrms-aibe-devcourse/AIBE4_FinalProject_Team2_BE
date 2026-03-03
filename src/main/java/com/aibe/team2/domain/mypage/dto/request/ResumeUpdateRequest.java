package com.aibe.team2.domain.mypage.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResumeUpdateRequest {

    @NotBlank(message = "자기소개서 제목은 필수 입력 항목입니다.")
    @Size(max = 255, message = "제목은 최대 255자까지 입력 가능합니다.")
    private String title;

    @NotEmpty(message = "자기소개서 본문 항목은 최소 1개 이상 작성해야 합니다.")
    @Valid
    private List<ResumeItemDto> items;

    @Getter
    @NoArgsConstructor
    public static class ResumeItemDto {

        // 1. 소제목 : 최대 500자
        @NotBlank(message = "소제목은 필수 입력 항목입니다.")
        @Size(max = 150, message = "소제목은 최대 150자까지 입력 가능합니다.")
        private String subtitle;

        // 2. 내용 : 100자 이상 1000자 이하
        @NotBlank(message = "내용은 필수 입력 항목입니다.")
        @Size(min = 100, max = 1000, message = "내용은 100자 이상 1000자 이하로 작성해야 합니다.")
        private String content;
    }
}
