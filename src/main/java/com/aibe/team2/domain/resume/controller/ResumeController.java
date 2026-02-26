package com.aibe.team2.domain.resume.controller;

import com.aibe.team2.domain.resume.dto.ResumeRequest;
import com.aibe.team2.domain.resume.dto.ResumeResponse;
import com.aibe.team2.domain.resume.service.ResumeService;
import com.aibe.team2.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    //Todo ResumeController & ResumeService memberId(currentMemberId)로 변경.

    // 1. 자기소개서 저장 (생성)
    @PostMapping
    public ApiResponse<ResumeResponse> createResume(@RequestBody @Valid ResumeRequest request) {
        log.info("Resume save requested. title: {}", request.title());
        ResumeResponse response = resumeService.saveResume(request);
        return ApiResponse.success(response);
    }

    // 2. 자기소개서 상세 조회
    @GetMapping("/{resumeId}")
    public ApiResponse<ResumeResponse> getResume(@PathVariable Long resumeId) {
        log.info("Resume lookup requested. id: {}", resumeId);
        ResumeResponse response = resumeService.findResume(resumeId);
        return ApiResponse.success(response);
    }

    // 3. 내 자기소개서 목록 보기(마이페이지)
    // 로그인 구현시
//    @GetMapping
//    public ApiResponse<List<ResumeResponse>> getMyResumes() {
//        Long currentMemberId = getLoginMemberId();
//        List<ResumeResponse> responses = resumeService.findMyResumes(currentMemberId);
//        return ApiResponse.success(responses);
//    }
}