package com.aibe.team2.domain.interview.service;

import com.aibe.team2.domain.interview.dto.RetellWebhookRequest;
import com.aibe.team2.domain.interview.entity.InterviewSession;
import com.aibe.team2.domain.interview.repository.InterviewSessionRepository;
import com.aibe.team2.domain.statistics.entity.InterviewRecord;
import com.aibe.team2.domain.statistics.repository.interview.InterviewRecordRepository;
import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewRecordService {

    private final InterviewRecordRepository interviewRecordRepository;
    private final InterviewSessionRepository interviewSessionRepository;

    @Transactional
    public void saveInterviewRecord(RetellWebhookRequest request) {
        // 1. Session ID 추출
        Map<String, Object> metadata = request.getCall().getMetadata();
        if(metadata == null || !metadata.containsKey("session_id")) {
            log.error("❌ 대화 기록 저장 실패: SQS 메시지에 session_id가 존재하지 않습니다.");
            throw new BusinessException(ErrorCode.COMMON_407); // 필수 값 누락 예외 발생
        }
        Long sessionId;

        try {
            sessionId = Long.valueOf(metadata.get("session_id").toString());
        } catch (NumberFormatException e) {
            log.error("❌ 대화 기록 저장 실패: 유효하지 않은 session_id 형식입니다. value: {}", metadata.get("session_id"));
            throw new BusinessException(ErrorCode.COMMON_406); // 타입 불일치 예외 발생
        }

        // 2. 부모 객체인 InterviewSession 조회
        InterviewSession session = interviewSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERVIEW_SESSION_NOT_FOUND));

        // 3. 대화 내역 추출 및 엔티티 변환
        List<InterviewRecord> recordsToSave = new ArrayList<>();
        List<RetellWebhookRequest.TranscriptTurn> turns = request.getCall().getTranscriptObject();
        int turnSequence = 1;

        for (int i = 0; i < turns.size(); i++) {
            RetellWebhookRequest.TranscriptTurn currentTurn = turns.get(i);
            String question = null;
            String answer = null;

            if ("agent".equals(currentTurn.getRole())) {
                // 현재 턴이 AI(agent)인 경우 -> 질문으로 설정
                question = currentTurn.getContent();

                // 다음 턴이 존재하고, 그 턴이 사용자(user)인 경우 -> 답변으로 묶음
                if (i + 1 < turns.size() && "user".equals(turns.get(i + 1).getRole())) {
                    answer = turns.get(i + 1).getContent();
                    i++; // 답변 턴을 '사용'했으므로 인덱스를 하나 건너뜁니다.
                }
            } else if ("user".equals(currentTurn.getRole())) {
                // AI 질문 없이 사용자가 먼저 말을 시작한 예외적인 경우
                answer = currentTurn.getContent();
            }

            // 하나의 InterviewRecord 객체로 생성 (질문과 답변이 한 쌍)
            recordsToSave.add(InterviewRecord.builder()
                    .interviewSession(session)
                    .turnSequence(turnSequence++)
                    .questionText(question)
                    .answerText(answer)
                    .build());
        }

        // 4. DB에 일괄 저장
        if (!recordsToSave.isEmpty()) {
            interviewRecordRepository.saveAll(recordsToSave);
            log.info("✅ 대화 기록 DB 저장 완료! Session ID: {}, 저장된 레코드 수: {}", sessionId, recordsToSave.size());
        }
    }
}
