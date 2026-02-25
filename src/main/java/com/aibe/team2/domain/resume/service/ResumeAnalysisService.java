package com.aibe.team2.domain.resume.service;

import com.aibe.team2.domain.jobposting.entity.JobPosting;
import com.aibe.team2.domain.jobposting.entity.JobSkill;
import com.aibe.team2.domain.jobposting.repository.JobPostingRepository;
import com.aibe.team2.domain.resume.entity.Resume;
import com.aibe.team2.domain.resume.entity.ResumeAnalysisReport;
import com.aibe.team2.domain.resume.repository.ResumeAnalysisRepository;
import com.aibe.team2.domain.resume.repository.ResumeRepository;
import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeAnalysisService {

    private final ResumeRepository resumeRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final JobPostingRepository jobPostingRepository;

    private final ResumeAnalysisAsyncWorker asyncWorker;
    private final AnalysisQueueProducer queueProducer;
    @Transactional
    public Long analyzeResume(Long resumeId) {
        // 1. 자소서 조회
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESUME_NOT_FOUND));

        // 2. 채용 공고 조회 (임시로 1번 공고 사용)
        Long defaultJobPostingId = 1L;
        JobPosting jobPosting = jobPostingRepository.findById(defaultJobPostingId)
                .orElseThrow(() -> new BusinessException(ErrorCode.JOB_POSTING_NOT_FOUND));

        // AI 임베딩 모델이 본문뿐만 아니라 요구 스킬까지 문맥으로 이해하고 코사인 유사도를 계산합니다!
        String jobSkillsText = jobPosting.getJobSkills().stream()
                .map(JobSkill::getSkillName)
                .collect(Collectors.joining(", "));

        String fullJobDescription = jobPosting.getJobDescription();
        if (!jobSkillsText.isEmpty()) {
            fullJobDescription += "\n\n[요구 기술 스택]\n" + jobSkillsText;
        }

        // 3. 기존 리포트 존재 여부 확인
        Optional<ResumeAnalysisReport> existingReport = resumeAnalysisRepository
                .findTopByResumeIdOrderByCreatedAtDesc(resumeId);

        ResumeAnalysisReport report;
        if (existingReport.isPresent() && existingReport.get().getJobPostingId().getId().equals(defaultJobPostingId)) {
            report = existingReport.get();
            report.startAnalysis(); // PROCESSING 상태로 변경
        } else {
            report = ResumeAnalysisReport.builder()
                    .resume(resume)
                    .jobPostingId(jobPosting)
                    .build();
            report.startAnalysis();
        }

        // 4. 상태를 PROCESSING으로 DB에 먼저 확정(Save) 합니다.
        ResumeAnalysisReport savedReport = resumeAnalysisRepository.save(report);

        // 5. 비동기 워커 호출 (스킬이 포함된 fullJobDescription을 넘깁니다)
        // 이전처럼 파라미터가 4개가 아니라 3개이므로 컴파일 에러가 발생하지 않습니다!
        queueProducer.sendAnalysisRequest(savedReport.getId(), resume.getContent(), fullJobDescription);

        // 6. 사용자(프론트엔드)에게는 즉시 리포트 ID를 반환하여 로딩 화면을 보여주게 합니다.
        return savedReport.getId();
    }

    @Transactional(readOnly = true)
    public ResumeAnalysisReport getAnalysisResult(Long resumeId) {
        return resumeAnalysisRepository.findTopByResumeIdOrderByCreatedAtDesc(resumeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ANALYSIS_REPORT_NOT_FOUND));
    }
}