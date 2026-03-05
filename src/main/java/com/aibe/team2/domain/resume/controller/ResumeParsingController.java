package com.aibe.team2.domain.resume.controller;

import com.aibe.team2.domain.resume.service.ResumeParsingEngine;
import com.aibe.team2.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/resumes")
@RequiredArgsConstructor
public class ResumeParsingController {

    private final ResumeParsingEngine resumeParsingEngine;


    // 이력서 파일(PDF, Word)을 업로드 받아 텍스트만 추출하여 반환합니다.
    @PostMapping("/extract")
    public ApiResponse<String> extractTextFromFile(@RequestParam("file") MultipartFile file) {

        // 엔진을 통해 텍스트 추출
        String extractedText = resumeParsingEngine.extractText(file);

        // 성공 응답으로 텍스트 반환 (프로젝트의 ApiResponse 포맷 사용)
        return ApiResponse.success(extractedText);
    }
}