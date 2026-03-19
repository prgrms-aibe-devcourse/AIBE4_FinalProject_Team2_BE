package com.aibe.team2.domain.interview.service;

import com.aibe.team2.domain.interview.dto.InterviewRequestDto;
import com.aibe.team2.domain.interview.dto.VoiceSessionResponse;
import com.aibe.team2.domain.interview.entity.InterviewSession;
import com.aibe.team2.domain.interview.enums.InterviewMode;
import com.aibe.team2.domain.interview.enums.InterviewSessionStatus;
import com.aibe.team2.domain.jobposting.entity.JobPosting;
import com.aibe.team2.domain.jobposting.repository.JobPostingRepository;
import com.aibe.team2.domain.resume.entity.Resume;
import com.aibe.team2.domain.resume.repository.ResumeRepository;
import com.aibe.team2.domain.statistics.entity.InterviewRecord;
import com.aibe.team2.domain.statistics.repository.interview.InterviewRecordRepository;
import com.aibe.team2.global.redis.lock.DistributedLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationManager {

    private final GeminiService geminiService;
    private final RetellService retellService;
    private final InterviewManager interviewManager; // 상태 변경을 위한 Manager 주입

    private final ResumeRepository resumeRepository; // [FR-INT-06] 이력서 조회를 위한 의존성 추가
    private final JobPostingRepository jobPostingRepository; //  [FR-INT-07] 채용 공고 조회를 위한 의존성 추가

    // 대화 기록을 DB에 저장하기 위한 Repository 주입
    private final InterviewRecordRepository recordRepository;

    @DistributedLock(key = "text-streaming", waitTime = 1, leaseTime = 20)
    public void startTextStreaming(InterviewSession session, String answer, String modelVariant, InterviewMode interviewMode, SseEmitter emitter) {

        // [FR-INT-06] 자기소개서 내용 안전하게 조회 및 추출
        String resumeContent = null;
        if (session.getResumeId() != null) {
            resumeContent = resumeRepository.findByIdAndMemberId(session.getResumeId(), session.getMemberId())
                    .map(Resume::getContent)
                    .orElse(null); // 권한이 없거나 찾을 수 없으면 주입하지 않음
        }

        // [FR-INT-07] 채용 공고 내용 안전하게 조회 및 추출
        String jobDescription = null;
        if (session.getJobPostingId() != null) {
            jobDescription = jobPostingRepository.findByIdAndMemberId(session.getJobPostingId(), session.getMemberId())
                    .map(JobPosting::getJobDescription)
                    .orElse(null);
        }

        // DTO 생성 시 추출한 이력서 및 공고 데이터 모두 포함
        InterviewRequestDto request = new InterviewRequestDto(answer, modelVariant, interviewMode, resumeContent, jobDescription);

        // [추가] AI 응답을 DB에 저장하기 위해 전체 문장을 담을 버퍼 생성
        StringBuilder fullAiResponse = new StringBuilder();

        geminiService.streamQuestion(String.valueOf(session.getId()), request).subscribe(
                data -> {
                    try {
                        fullAiResponse.append(data); // [추가] 스트리밍 데이터 누적
                        emitter.send(SseEmitter.event().name("message").data(data));
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                },
                error -> {
                    log.error("스트리밍 중 에러 발생. 세션을 ABORTED 상태로 전환합니다. SessionID: {}", session.getId(), error);
                    // 통계 제외를 위해 에러 발생 시 상태를 ABORTED로 변경
                    interviewManager.advanceStatus(session.getId(), InterviewSessionStatus.ABORTED);
                    emitter.completeWithError(error);
                },
                () -> {
                    // [추가] 스트리밍이 정상적으로 완료되면 누적된 데이터를 DB에 저장 (이후 리포트 분석에 사용)
                    saveTextConversation(session, answer, fullAiResponse.toString());
                    emitter.complete();
                }
        );
    }

    // 지원자의 답변과 AI의 다음 질문을 하나의 InterviewRecord로 묶어 저장
    private void saveTextConversation(InterviewSession session, String userAnswer, String aiQuestion) {
        try {
            // 현재 세션의 기존 기록 개수를 확인하여 순서 결정
            List<InterviewRecord> existingRecords = recordRepository.findAllByInterviewSessionIdOrderByTurnSequenceAsc(session.getId());
            int nextTurn = existingRecords.size() + 1;

            InterviewRecord record = InterviewRecord.builder()
                    .interviewSession(session)
                    .turnSequence(nextTurn)
                    .questionText(aiQuestion)
                    .answerText(userAnswer)
                    .build();

            recordRepository.save(record);
            log.info("💾 [DB 저장] 텍스트 대화 기록 저장 완료 - Session: {}, Turn: {}", session.getId(), nextTurn);
        } catch (Exception e) {
            log.error("❌ [DB 저장 실패] 대화 기록 저장 중 에러: {}", e.getMessage());
        }
    }

    public VoiceSessionResponse startVoiceInterview(Long sessionId) {
        return retellService.createVoiceCall(sessionId);
    }
}