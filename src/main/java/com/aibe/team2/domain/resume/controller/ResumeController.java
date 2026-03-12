package com.aibe.team2.domain.resume.controller;

import com.aibe.team2.domain.mypage.dto.request.ResumeUpdateRequest;
import com.aibe.team2.domain.resume.dto.ResumeRequest;
import com.aibe.team2.domain.resume.dto.ResumeResponse;
import com.aibe.team2.domain.resume.service.ResumeService;
import com.aibe.team2.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    //Todo ResumeController & ResumeService memberId(currentMemberId)로 변경.

    // 1. 자기소개서 저장 (생성)
    @PostMapping
    public ApiResponse<ResumeResponse> createResume(@RequestBody @Valid ResumeRequest request) {
        log.info("Resume save requested. title: {}", request.title());
        // 성공 시
        log.info("Resume save success. title: {}", request.title());
        // 하드코딩중 로그인 연동 후 수정
        Long currentMemberId = 1L;
        ResumeResponse response = resumeService.saveResume(currentMemberId, request);
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
    // 로그인 구현시 currentMemberId 수정
    @GetMapping
    public ApiResponse<List<ResumeResponse>> getMyResumes() {
        // 하드코딩중 로그인 연동 후 수정
        Long currentMemberId = 1L;

        // 서비스 호출 및 응답 반환
        List<ResumeResponse> responseList = resumeService.getMyResumes(currentMemberId);
        return ApiResponse.success(responseList);
    }

    // 4. 자기소개서 수정
    @PatchMapping("{resumeId}")
    public ApiResponse<Long> updateResume(
            @PathVariable Long resumeId,
            @RequestBody @Valid ResumeUpdateRequest request
    ) {
        // TODO : Spring Security 구현 후 수정
        Long currentMemberId = 1L;
        log.info("Resume update requested. resumeId: {}, memberId: {}", resumeId, currentMemberId);

        // 1. 서비스 로직 호출
        resumeService.updateResume(resumeId, currentMemberId, request);

        // 2. 성공시 수정된 자기소개서의 ID 반환
        return ApiResponse.success(resumeId);
    }
}