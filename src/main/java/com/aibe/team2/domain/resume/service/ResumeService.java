package com.aibe.team2.domain.resume.service;

import com.aibe.team2.domain.mypage.dto.request.ResumeUpdateRequest;
import com.aibe.team2.domain.resume.dto.ResumeRequest;
import com.aibe.team2.domain.resume.dto.ResumeResponse;
import com.aibe.team2.domain.resume.entity.Resume;
import com.aibe.team2.domain.resume.repository.ResumeRepository;
import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.BusinessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final ObjectMapper objectMapper;

    // 1. 자기소개서 저장
    @Transactional
    public ResumeResponse saveResume(ResumeRequest request) {
        Resume resume = Resume.builder()
                .memberId(request.memberId())
                .title(request.title())
                .content(request.content())
                .s3FileUrl(null)
                .build();

        Resume savedResume = resumeRepository.save(resume);
        return ResumeResponse.from(savedResume);
    }

    // 2. 자기소개서 상세 조회
    public ResumeResponse findResume(Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESUME_NOT_FOUND));

        return ResumeResponse.from(resume);
    }

    // 3. 자기소개서 목록 보기
    // 로그인 구현시
//    @GetMapping
//    public ApiResponse<List<ResumeResponse>> getMyResumes() {
//        Long currentMemberId = getLoginMemberId();
//        List<ResumeResponse> responses = resumeService.findMyResumes(currentMemberId);
//        return ApiResponse.success(responses);
//    }

    // [추가] 자기소개서 수정 로직
    // 4. 자기소개서 수정
    @Transactional
    public void updateResume(Long resumeId, Long memberId, ResumeUpdateRequest request) {

        // 엔티티 조회 및 권한 검증
        Resume resume = resumeRepository.findByIdAndMemberId(resumeId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESUME_NOT_FOUND));
        // 데이터 구조 변환 및 엔티티 수정
        try {
            String contentJson = objectMapper.writeValueAsString(request.getItems());
            resume.update(request.getTitle(), contentJson);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.COMMON_JSON_CONVERSION_ERROR);
        }
    }
}