package com.aibe.team2.domain.resume.service;

import com.aibe.team2.domain.resume.entity.Resume;
import com.aibe.team2.domain.resume.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeAsyncService {

    private final ResumeRepository resumeRepository;
    private final SimilarityEngine similarityEngine;
    @Async
    @Transactional
    public void updateEmbeddingAsync(Long resumeId, String contentJson) {
        try {
            float[] originalEmbedding = similarityEngine.getEmbeddingAsFloatArray(contentJson);
            float[] finalEmbedding = originalEmbedding.length > 768 ? Arrays.copyOf(originalEmbedding, 768) : originalEmbedding;

            Resume resume = resumeRepository.findById(resumeId)
                    .orElseThrow(() -> new IllegalArgumentException("이력서를 찾을 수 없습니다. ID: " + resumeId));

            resume.updateEmbeddingOnly(finalEmbedding);
            log.info("✅ 백그라운드 임베딩 업데이트 완료 - resumeId: {}", resumeId);
        } catch (Exception e) {
            log.error("❌ 임베딩 비동기 업데이트 실패 - resumeId: {}, 원인: {}", resumeId, e.getMessage());
        }
    }
}