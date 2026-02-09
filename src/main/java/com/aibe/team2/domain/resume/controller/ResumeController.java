package com.aibe.team2.domain.resume.controller;

import com.aibe.team2.domain.resume.dto.ResumeRequest;
import com.aibe.team2.domain.resume.dto.ResumeResponse;
import com.aibe.team2.domain.resume.service.ResumeService;
import com.aibe.team2.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 아래의 코드는 전부 예시입니다. 추후 기능이나 필요에 맞게 자유롭게 수정하면 됩니다.
 */
@Slf4j // 컨벤션: System.out 금지, 로그 사용
@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping
    public ApiResponse<ResumeResponse> createResume(@RequestBody ResumeRequest request) {
        // 컨벤션: 명확한 변수 네이밍 및 로그 기록
        log.info("Resume analysis requested: {}", request.title());

        ResumeResponse response = resumeService.saveAndAnalyze(request);

        return ApiResponse.success(response);
    }
}