package com.aibe.team2.domain.resume.controller;

import com.aibe.team2.domain.mypage.dto.request.ResumeUpdateRequest;
import com.aibe.team2.domain.resume.dto.ResumeRequest;
import com.aibe.team2.domain.resume.dto.ResumeResponse;
import com.aibe.team2.domain.resume.service.ResumeService;
import com.aibe.team2.global.common.annotation.LoginMemberId;
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

    // 1. 자기소개서 저장 (생성)
    @PostMapping
    public ApiResponse<ResumeResponse> createResume(
            @LoginMemberId Long memberId, // 변경: 하드코딩 제거 및 어노테이션 적용
            @RequestBody @Valid ResumeRequest request
    ) {
        log.info("Resume save requested. title: {}", request.title());

        ResumeResponse response = resumeService.saveResume(memberId, request);
        return ApiResponse.success(response);
    }

    // 2. 자기소개서 상세 조회
    @GetMapping("/{resumeId}")
    public ApiResponse<ResumeResponse> getResume(
            @LoginMemberId Long memberId,
            @PathVariable Long resumeId
    ) {
        log.info("Resume lookup requested. resumeId: {}, memberId: {}", resumeId, memberId);
        ResumeResponse response = resumeService.findResume(resumeId, memberId);
        return ApiResponse.success(response);
    }

    // 3. 내 자기소개서 목록 보기(마이페이지)
    @GetMapping
    public ApiResponse<List<ResumeResponse>> getMyResumes(
            @LoginMemberId Long memberId
    ) {
        List<ResumeResponse> responseList = resumeService.getMyResumes(memberId);
        return ApiResponse.success(responseList);
    }

    // 4. 자기소개서 수정
    @PatchMapping("/{resumeId}")
    public ApiResponse<Long> updateResume(
            @LoginMemberId Long memberId,
            @PathVariable Long resumeId,
            @RequestBody @Valid ResumeUpdateRequest request
    ) {
        log.info("Resume update requested. resumeId: {}, memberId: {}", resumeId, memberId);

        // 1. 서비스 로직 호출
        resumeService.updateResume(resumeId, memberId, request);

        // 2. 성공시 수정된 자기소개서의 ID 반환
        return ApiResponse.success(resumeId);
    }
}