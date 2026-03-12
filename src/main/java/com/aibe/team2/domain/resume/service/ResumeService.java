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
    private final SimilarityEngine similarityEngine;

    // 1. 자기소개서 저장
    @Transactional
    public ResumeResponse saveResume(Long memberId, ResumeRequest request) {

        // 자기소개서 내용(Text)을 Vector(숫자 배열)로 변환
        float[] embedding = getEmbeddingForText(request.content());

        Resume resume = Resume.builder()
                .memberId(memberId)
                .title(request.title())
                .content(request.content())
                .embedding(embedding) // 변환된 벡터 저장
                .build();

        Resume savedResume = resumeRepository.save(resume);
        return ResumeResponse.from(savedResume);
    }

    // 2. 자기소개서 상세 조회
    public ResumeResponse findResume(Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId).orElseThrow(() -> new BusinessException(ErrorCode.RESUME_NOT_FOUND));
        return ResumeResponse.from(resume);
    }

    // 3. 내 이력서 목록 조회
    public List<ResumeResponse> getMyResumes(Long memberId) {
        List<Resume> resumes = resumeRepository.findAllByMemberId(memberId);
        return resumes.stream().map(ResumeResponse::from).collect(Collectors.toList());
    }

    // 4. 자기소개서 수정
    @Transactional
    public void updateResume(Long resumeId, Long memberId, ResumeUpdateRequest request) {
        Resume resume = resumeRepository.findByIdAndMemberId(resumeId, memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESUME_NOT_FOUND));

        try {
            String contentJson = objectMapper.writeValueAsString(request.getItems());

            // ★ 수정된 본문 내용을 바탕으로 새로운 Vector 생성
            float[] newEmbedding = getEmbeddingForText(contentJson);

            resume.update(request.getTitle(), contentJson, newEmbedding); // 엔티티 수정 (더티체킹)
        } catch (JsonProcessingException e) {
            log.error("자기소개서 항목 JSON 직렬화 실패 - resumeId: {}, error: {}", resumeId, e.getMessage(), e);
            throw new BusinessException(ErrorCode.COMMON_JSON_CONVERSION_ERROR);
        }
    }

    // Helper: 텍스트를 받아서 float 배열(벡터)로 반환
    private float[] getEmbeddingForText(String text) {
        return similarityEngine.getEmbeddingAsFloatArray(text);
    }
}