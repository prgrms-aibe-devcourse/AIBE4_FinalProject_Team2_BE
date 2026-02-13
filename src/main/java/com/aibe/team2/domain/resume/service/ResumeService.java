package com.aibe.team2.domain.resume.service;

import com.aibe.team2.domain.resume.dto.ResumeRequest;
import com.aibe.team2.domain.resume.dto.ResumeResponse;
import com.aibe.team2.domain.resume.entity.Resume;
import com.aibe.team2.domain.resume.repository.ResumeRepository;
import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.custom.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResumeService {

    private final ResumeRepository resumeRepository;

    // 1. 이력서 저장
    @Transactional
    public ResumeResponse saveResume(ResumeRequest request) {
        Resume resume = Resume.builder()
                .userId(request.userId())
                .title(request.title())
                .content(request.content())
                .s3FileUrl(null)
                .build();

        Resume savedResume = resumeRepository.save(resume);
        return ResumeResponse.from(savedResume);
    }

    // 2. 이력서 상세 조회 (추가됨)
    public ResumeResponse findResume(Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.RESUME_NOT_FOUND)); // 에러 코드 확인 필요

        return ResumeResponse.from(resume);
    }
}