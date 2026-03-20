package com.aibe.team2.domain.interview.service;

import com.aibe.team2.domain.interview.dto.InterviewReportResponse;
import com.aibe.team2.domain.interview.dto.InterviewReportResponse.RecordDto;
import com.aibe.team2.domain.interview.entity.InterviewSession;
import com.aibe.team2.domain.interview.enums.InterviewSessionStatus;
import com.aibe.team2.domain.jobposting.entity.JobPosting;
import com.aibe.team2.domain.jobposting.repository.JobPostingRepository;
import com.aibe.team2.domain.resume.entity.Resume;
import com.aibe.team2.domain.resume.repository.ResumeRepository;
import com.aibe.team2.domain.statistics.entity.InterviewRecord;
import com.aibe.team2.domain.statistics.repository.interview.InterviewRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InterviewReportService {

    private final ResumeRepository resumeRepository;
    private final JobPostingRepository jobPostingRepository;
    // 대화 기록을 조회하기 위한 Repository 주입
    private final InterviewRecordRepository interviewRecordRepository;

    public InterviewReportResponse getReport(InterviewSession session) {

        // [FR-INT-08] 핵심: DONE 상태 세션만 리포트 조회가 가능해야 한다.
        if (session.getStatus() != InterviewSessionStatus.DONE) {
            throw new IllegalStateException("면접이 정상적으로 완료된(DONE) 세션만 리포트를 조회할 수 있습니다. 현재 상태: " + session.getStatus());
        }

        // 이력서 제목 조회
        String resumeTitle = null;
        if (session.getResumeId() != null) {
            resumeTitle = resumeRepository.findByIdAndMemberId(session.getResumeId(), session.getMemberId())
                    .map(Resume::getTitle)
                    .orElse("삭제된 이력서");
        }

        // 채용 공고 제목 조회
        String jobTitle = null;
        if (session.getJobPostingId() != null) {
            jobTitle = jobPostingRepository.findByIdAndMemberId(session.getJobPostingId(), session.getMemberId())
                    .map(JobPosting::getJobTitle)
                    .orElse("삭제된 공고");
        }

        // 해당 세션에 속한 모든 대화 기록(Turn)을 순서대로 조회
        List<InterviewRecord> records = interviewRecordRepository.findAllByInterviewSessionIdOrderByTurnSequenceAsc(session.getId());

        // 엔티티 리스트(InterviewRecord)를 DTO 리스트(RecordDto)로 변환
        List<RecordDto> recordDtos = records.stream()
                .map(record -> RecordDto.builder()
                        .turnSequence(record.getTurnSequence())
                        .questionText(record.getQuestionText())
                        .answerText(record.getAnswerText())
                        .evaluationScore(record.getEvaluationScore()) // 턴별 점수
                        .aiFeedback(record.getFeedbackText())         // 턴별 AI 피드백 매핑
                        .build())
                .collect(Collectors.toList());

        // 완성된 RecordDto 리스트를 of 메서드의 4번째 인자로 넘겨서 반환
        return InterviewReportResponse.of(session, resumeTitle, jobTitle, recordDtos);
    }
}