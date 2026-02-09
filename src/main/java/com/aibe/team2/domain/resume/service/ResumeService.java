package com.aibe.team2.domain.resume.service;

import com.aibe.team2.domain.resume.dto.ResumeRequest;
import com.aibe.team2.domain.resume.dto.ResumeResponse;
import com.aibe.team2.domain.resume.entity.Resume;
import com.aibe.team2.domain.resume.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 아래의 코드는 전부 예시입니다. 추후 기능이나 필요에 맞게 자유롭게 수정하면 됩니다.
 */
@Service
@RequiredArgsConstructor // 컨벤션: 생성자 주입
@Transactional(readOnly = true)
public class ResumeService {

    private final ResumeRepository resumeRepository;

    @Transactional
    public ResumeResponse saveAndAnalyze(ResumeRequest request) {
        // TODO: AI Integration Track - 외부 AI API(OpenAI 등) 호출 로직이 들어올 위치입니다.
        String dummyFeedback = "컨벤션에 맞춘 AI 분석 결과 예시입니다.";

        Resume resume = Resume.builder()
                .title(request.title())
                .content(request.content())
                .feedback(dummyFeedback)
                .build();

        Resume savedResume = resumeRepository.save(resume);
        return ResumeResponse.from(savedResume);
    }
}