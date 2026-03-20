package com.aibe.team2.domain.interview.service;

import com.aibe.team2.domain.interview.dto.AnalysisResultDto;
import com.aibe.team2.domain.interview.entity.InterviewSession;
import com.aibe.team2.domain.interview.enums.InterviewSessionStatus;
import com.aibe.team2.domain.interview.repository.InterviewSessionRepository;
import com.aibe.team2.domain.statistics.entity.InterviewRecord;
import com.aibe.team2.domain.statistics.repository.interview.InterviewRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewAnalysisService {

    private final InterviewSessionRepository sessionRepository;
    private final InterviewRecordRepository recordRepository;
    private final GeminiAnalysisService geminiAnalysisService;
    private final ObjectMapper objectMapper;

    // 면접이 종료된 세션을 대상으로 AI 분석을 수행
    @Async
    @Transactional
    public void analyzeSession(Long sessionId) {
        log.info("🔍 면접 분석 프로세스 시작 - SessionID: {}", sessionId);

        InterviewSession session = sessionRepository.findById(sessionId).orElse(null);
        if (session == null || session.getStatus() != InterviewSessionStatus.DONE) {
            log.warn("분석 스킵: 세션이 없거나 완료(DONE) 상태가 아닙니다. SessionID: {}", sessionId);
            return;
        }

        List<InterviewRecord> records = recordRepository.findAllByInterviewSessionIdOrderByTurnSequenceAsc(sessionId);
        if (records.isEmpty()) {
            log.warn("분석 스킵: 해당 세션에 저장된 면접 기록이 없습니다. SessionID: {}", sessionId);
            return;
        }

        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("--- [면접 대화 로그] ---\n");
        for (InterviewRecord record : records) {
            promptBuilder.append("순서: ").append(record.getTurnSequence()).append("\n")
                    .append("질문: ").append(record.getQuestionText() != null ? record.getQuestionText() : "질문 없음").append("\n")
                    .append("답변: ").append(record.getAnswerText() != null ? record.getAnswerText() : "답변 없음").append("\n\n");
        }

        try {
            String jsonResult = geminiAnalysisService.analyzeInterviewSync(promptBuilder.toString());
            AnalysisResultDto analysisResult = objectMapper.readValue(jsonResult, AnalysisResultDto.class);

            // [PR 리뷰 반영] DTO 객체를 직접 전달하여 호출부를 깔끔하게 리팩토링
            if (analysisResult.getTotalScore() != null) {
                session.updateAnalysisResult(analysisResult);
            }

            // 개별 질문/답변에 대한 턴별 AI 피드백 저장
            if (analysisResult.getTurnScripts() != null) {
                for (AnalysisResultDto.RecordAnalysis ra : analysisResult.getTurnScripts()) {
                    records.stream()
                            .filter(r -> r.getTurnSequence().equals(ra.getTurnSequence()))
                            .findFirst()
                            .ifPresent(r -> {
                                float score = ra.getEvaluationScore() != null ? ra.getEvaluationScore().floatValue() : 0f;
                                r.updateAIAnalysis(score, ra.getAiFeedback());
                            });
                }
            }

            log.info("✅ 분석 결과 전체 저장 완료 - SessionID: {}, 총점: {}", sessionId, analysisResult.getTotalScore());

        } catch (Exception e) {
            // [PR 리뷰 반영] e.getMessage() 대신 e 전체를 넘겨 스택 트레이스를 로깅하도록 수정
            log.error("❌ 면접 분석 중 오류 발생 - SessionID: {}", sessionId, e);
        }
    }
}