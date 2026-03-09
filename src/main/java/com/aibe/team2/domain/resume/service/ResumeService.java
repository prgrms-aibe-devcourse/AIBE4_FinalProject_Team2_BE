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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final ObjectMapper objectMapper;

    // 1. 자기소개서 저장
    @Transactional
    public ResumeResponse saveResume(Long memberId, ResumeRequest request) {

        Resume resume = Resume.builder()
                .memberId(memberId)
                .title(request.title())
                .content(request.content())
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


    public List<ResumeResponse> getMyResumes(Long memberId) {
        // 1. 유저 ID로 이력서 목록 조회
        // 하드코딩 개발용
        // Long memberId = 1L;
        List<Resume> resumes = resumeRepository.findAllByMemberId(memberId);

        // 2. Entity 리스트를 DTO 리스트로 변환하여 반환
        return resumes.stream()
                .map(ResumeResponse::from)
                .collect(Collectors.toList());
    }

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
            log.error("자기소개서 항목 JSON 직렬화 실패 - resumeId: {}, error: {}", resumeId, e.getMessage(), e);
            throw new BusinessException(ErrorCode.COMMON_JSON_CONVERSION_ERROR);
        }
    }
}