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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationManager {

    private final GeminiService geminiService;
    private final RetellService retellService;
    private final InterviewManager interviewManager; // 상태 변경을 위한 Manager 주입

    private final ResumeRepository resumeRepository; // [FR-INT-06] 이력서 조회를 위한 의존성 추가
    private final JobPostingRepository jobPostingRepository; //  [FR-INT-07] 채용 공고 조회를 위한 의존성 추가
    private final InterviewRecordRepository recordRepository; // 대화 기록을 DB에 저장하기 위한 Repository 주입

    // JSON 파싱을 위해 ObjectMapper 주입
    private final ObjectMapper objectMapper;

    // 각 세션별로 이전 턴의 AI 질문을 기억하기 위한 스레드 안전한 인메모리 캐시 도입
    private final Map<Long, String> previousQuestionCache = new ConcurrentHashMap<>();

    // 프론트엔드와 100% 동일한 동적 첫 인사말 생성 헬퍼 메서드
    private String getInitialGreeting(InterviewMode mode) {
        if (mode == InterviewMode.STRESS) {
            return "바로 시작하겠습니다. 지원자님, 1분 자기소개 해보세요.";
        } else if (mode == InterviewMode.FOLLOW_UP) {
            return "지원해 주셔서 감사합니다. 먼저 본인의 핵심 역량을 중심으로 자기소개를 부탁드립니다.";
        } else {
            return "반갑습니다! 긴장 푸시고 편하게 자기소개 부탁드립니다.";
        }
    }

    @DistributedLock(key = "text-streaming", waitTime = 1, leaseTime = 20)
    public void startTextStreaming(InterviewSession session, String answer, String modelVariant, InterviewMode interviewMode, SseEmitter emitter) {
        // 하드코딩된 인사말 대신, 세션의 면접 모드에 맞는 동적 인사말 가져옴
        String previousQuestion = previousQuestionCache.getOrDefault(session.getId(), getInitialGreeting(session.getInterviewMode()));

        // [FR-INT-06] 자기소개서 내용 안전하게 조회 및 추출
        String resumeContent = null;
        if (session.getResumeId() != null) {
            resumeContent = resumeRepository.findByIdAndMemberId(session.getResumeId(), session.getMemberId())
                    .map(Resume::getContent)
                    .orElse(null); // 권한이 없거나 찾을 수 없으면 주입하지 않음
        }

        // [FR-INT-07] 채용 공고 내용 안전하게 조회 및 추출
        String jobDescription = session.getJobDescription();

        // 커스텀 텍스트가 없고 기존 공고 ID가 있다면 DB에서 찾아오기
        if (jobDescription == null && session.getJobPostingId() != null) {
            jobDescription = jobPostingRepository.findById(session.getJobPostingId())
                    .map(JobPosting::getJobDescription)
                    .orElse(null);
        }

        // [수정] DTO 생성 시 세션에 저장된 직무(jobRole)와 연차(experience)도 함께 전달
        InterviewRequestDto request = new InterviewRequestDto(
                answer, modelVariant, interviewMode, resumeContent, jobDescription,
                session.getJobRole(), session.getExperience()
        );

        // 원본 JSON 문자열 누적 대신, 파싱된 순수 텍스트만 누적하도록 변경
        StringBuilder cleanAiResponse = new StringBuilder();

        geminiService.streamQuestion(String.valueOf(session.getId()), request).subscribe(
                data -> {
                    try {
                        // 프론트엔드로는 원본 데이터 전송 (기존 스트리밍 작동 유지)
                        emitter.send(SseEmitter.event().name("message").data(data));

                        // DB 저장을 위해 JSON 청크에서 텍스트만 추출하여 누적
                        String parsedText = extractTextFromChunk(data);
                        if (parsedText != null && !parsedText.isEmpty()) {
                            cleanAiResponse.append(parsedText);
                        }
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
                    // 완료 시 정제된 순수 텍스트(cleanAiResponse)만 DB에 저장
                    saveTextConversation(session, previousQuestion, answer);
                    // 이번 스트리밍을 통해 새롭게 생성된 AI의 질문은 다음 턴을 위해 캐시에 임시 저장
                    previousQuestionCache.put(session.getId(), cleanAiResponse.toString());
                    emitter.complete();
                }
        );
    }

    //스트리밍되는 JSON 청크 데이터에서 순수 텍스트만 안전하게 추출하는 헬퍼 메서드
    private String extractTextFromChunk(String data) {
        try {
            JsonNode rootNode = objectMapper.readTree(data);
            JsonNode candidates = rootNode.path("candidates");
            if (candidates.isArray() && candidates.size() > 0) {
                JsonNode parts = candidates.get(0).path("content").path("parts");
                if (parts.isArray() && parts.size() > 0) {
                    return parts.get(0).path("text").asText("");
                }
            }
            if (rootNode.has("text")) {
                return rootNode.get("text").asText("");
            }
        } catch (Exception e) {
            // 파싱 실패 시 원본 데이터가 깨진 청크일 수 있으므로 조용히 무시합니다.
        }
        return "";
    }

    // 지원자의 답변과 AI의 다음 질문을 하나의 InterviewRecord로 묶어 저장
    private void saveTextConversation(InterviewSession session, String question, String answer) {
        try {
            // 현재 세션의 기존 기록 개수를 확인하여 순서 결정
            List<InterviewRecord> existingRecords = recordRepository.findAllByInterviewSessionIdOrderByTurnSequenceAsc(session.getId());
            int nextTurn = existingRecords.size() + 1;

            InterviewRecord record = InterviewRecord.builder()
                    .interviewSession(session)
                    .turnSequence(nextTurn)
                    .questionText(question) // 정상적으로 짝이 맞춰진 질문
                    .answerText(answer) // 사용자의 해당 답변
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