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

import java.util.Arrays; // 🔴 Arrays 임포트 추가
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
    private final ResumeAsyncService resumeAsyncService;

    // 1. 자기소개서 저장
    @Transactional
    public ResumeResponse saveResume(Long memberId, ResumeRequest request) {

        // 자기소개서 내용(Text)을 Vector(숫자 배열)로 변환
        float[] embedding = getEmbeddingForText(request.content());

        Resume resume = Resume.builder()
                .memberId(memberId)
                .title(request.title())
                .content(request.content())
                .embedding(embedding)
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

            // 1. 텍스트 즉시 저장 (사용자는 대기 없이 1초 만에 수정 완료됨)
            resume.updateTextOnly(request.getTitle(), contentJson);

            // 2. 비동기로 임베딩 업데이트 작업 던져놓고 메서드 종료
            resumeAsyncService.updateEmbeddingAsync(resumeId, contentJson);

        } catch (JsonProcessingException e) {
            log.error("자기소개서 항목 JSON 직렬화 실패 - resumeId: {}, error: {}", resumeId, e.getMessage());
            throw new BusinessException(ErrorCode.COMMON_JSON_CONVERSION_ERROR);
        }
    }

    private float[] getEmbeddingForText(String text) {
        float[] originalEmbedding = similarityEngine.getEmbeddingAsFloatArray(text);

        // 이미 768차원이면 그대로 리턴
        if (originalEmbedding.length == 768) {
            return originalEmbedding;
        }

        // 3072차원 등 768차원보다 큰 배열이 들어오면 앞에서부터 딱 768개만 잘라냄
        if (originalEmbedding.length > 768) {
            log.info("임베딩 차원이 {}입니다. DB 스키마에 맞게 768차원으로 절삭합니다.", originalEmbedding.length);
            return Arrays.copyOf(originalEmbedding, 768);
        }

        throw new IllegalArgumentException("생성된 임베딩 차원이 너무 작습니다: " + originalEmbedding.length);
    }
}