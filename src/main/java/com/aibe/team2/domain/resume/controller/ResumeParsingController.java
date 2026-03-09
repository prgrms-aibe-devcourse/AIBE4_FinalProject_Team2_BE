package com.aibe.team2.domain.resume.controller;

import com.aibe.team2.domain.resume.dto.ResumeParsedItem;
import com.aibe.team2.domain.resume.service.ResumeParsingEngine;
import com.aibe.team2.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/resumes")
@RequiredArgsConstructor
public class ResumeParsingController {

    private final ResumeParsingEngine resumeParsingEngine;

    // 이력서 파일(PDF, Word, HWPX)을 업로드 받아 소제목과 내용으로 구조화하여 반환합니다.
    @PostMapping("/extract")
    public ApiResponse<List<ResumeParsedItem>> extractTextFromFile(@RequestParam("file") MultipartFile file) {

        // 엔진을 통해 텍스트를 추출하고 정규식으로 소제목/내용 분리
        List<ResumeParsedItem> extractedItems = resumeParsingEngine.extractAndSplitText(file);

        // 성공 응답으로 구조화된 리스트 반환
        return ApiResponse.success(extractedItems);
    }
}