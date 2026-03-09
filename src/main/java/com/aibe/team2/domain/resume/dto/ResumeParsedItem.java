package com.aibe.team2.domain.resume.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResumeParsedItem {
    private String subtitle; // 14pt로 보여줄 소제목
    private String content;  // 10pt로 보여줄 내용
}