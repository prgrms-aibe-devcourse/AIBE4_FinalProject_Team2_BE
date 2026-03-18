package com.aibe.team2.domain.interview.service;

import com.aibe.team2.domain.interview.dto.RetellWebhookRequest;
import com.aibe.team2.domain.interview.entity.InterviewSession;
import com.aibe.team2.domain.interview.repository.InterviewSessionRepository;
import com.aibe.team2.domain.statistics.entity.InterviewRecord;
import com.aibe.team2.domain.statistics.repository.interview.InterviewRecordRepository;
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
            log.error("❌ 대화 기록 저장 실패: session_id가 존재하지 않습니다.");
            return;
        }
        Long sessionId = Long.valueOf(metadata.get("session_id").toString());

        // 2. 부모ㅓ 객체인 InterviewSession 조회
        InterviewSession session = interviewSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("해당 세션을 찾을 수 없습니다. : " + sessionId));

        // 3. 대화 내역 추출 및 엔티티 변환
        List<InterviewRecord> recordsToSave = new ArrayList<>();

        int turnSequence = 1;
        for (RetellWebhookRequest.TranscriptTurn turn : request.getCall().getTranscriptObject()) {
            InterviewRecord record = InterviewRecord.builder()
                    .interviewSession(session)
                    .turnSequence(turnSequence++)
                    .questionText(turn.getRole().equals("agent") ? turn.getContent() : null) // AI가 말한 거면 질문에
                    .answerText(turn.getRole().equals("user") ? turn.getContent() : null)    // 사람이 말한 거면 답변에
                    .build();
            recordsToSave.add(record);
        }

        // 4. DB에 일괄 저장
        if(!recordsToSave.isEmpty()) {
            interviewRecordRepository.saveAll(recordsToSave);
            log.info("✅ 대화 기록 DB 저장 완료! Session ID: {}, 저장된 Turn 수: {}", sessionId, recordsToSave.size());
        }
    }
}
