package com.aibe.team2.domain.statistics.service;

import com.aibe.team2.domain.interview.entity.InterviewSession;
import com.aibe.team2.domain.interview.repository.InterviewRepository;
import com.aibe.team2.domain.statistics.dto.interview.InterviewResultDetailResponse;
import com.aibe.team2.domain.statistics.dto.interview.InterviewResultDetailResponse.LogicAndStructure;
import com.aibe.team2.domain.statistics.dto.interview.InterviewResultDetailResponse.SpeechAnalysis;
import com.aibe.team2.domain.statistics.dto.interview.InterviewResultDetailResponse.TurnScript;
import com.aibe.team2.domain.statistics.entity.InterviewRecord;
import com.aibe.team2.domain.statistics.entity.InterviewResultStatistics;
import com.aibe.team2.domain.statistics.repository.InterviewRecordRepository;
import com.aibe.team2.domain.statistics.repository.InterviewResultStatisticsRepository;
import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.custom.ForbiddenException;
import com.aibe.team2.global.exception.custom.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
// TODO : 하드코딩 제거
public class InterviewStatisticsService {

    private final InterviewRepository interviewRepository;
    private final InterviewRecordRepository recordRepository;
    private final InterviewResultStatisticsRepository statisticsRepository;

    @Transactional(readOnly = true)
    public InterviewResultDetailResponse getInterviewStatistics(Long sessionId, Long currentMemberId) {

        // 1. 세션 조회
        InterviewSession session = interviewRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COMMON_404));

        // 2. 권한 검증 (스키마에 맞춰 getMemberId() 사용)
        if (!session.getMemberId().equals(currentMemberId)) {
            throw new ForbiddenException(ErrorCode.COMMON_403);
        }

        // 3. 기록 조회
        List<InterviewRecord> records = recordRepository.findAllByInterviewSessionIdOrderByTurnSequenceAsc(sessionId);

        // 4. 통계 엔티티 조회
        InterviewResultStatistics stats = statisticsRepository.findByInterviewSessionId(sessionId).orElse(null);

        // 5. 로직 및 구조 지표 매핑
        LogicAndStructure logicAndStructure = new LogicAndStructure(
                stats != null && stats.getAvgClarity() != null ? stats.getAvgClarity().intValue() : 0,
                stats != null && stats.getAvgPersuasiveness() != null ? stats.getAvgPersuasiveness().intValue() : 0,
                stats != null && stats.getAvgConsistency() != null ? stats.getAvgConsistency().intValue() : 0
        );

        // 6. 비언어 통계 집계 (메서드명 및 타입 매칭)
        SpeechAnalysis speechAnalysis = calculateSpeechAnalysis(records);

        // 7. 턴별 스크립트 매핑 (파라미터 10개 매칭)
        List<TurnScript> turnScripts = records.stream()
                .map(record -> new TurnScript(
                        record.getTurnSequence(),
                        record.getQuestionText(),
                        record.getAnswerText(),
                        record.getFeedbackText(),
                        record.getEvaluationScore() != null ? record.getEvaluationScore().doubleValue() : 0.0,
                        Collections.emptyList(),
                        record.getWpm(),
                        record.getSilenceCount(),
                        record.getSttAccuracy(),
                        record.getEmotionAnalysis(),
                        false
                ))
                .toList();

        // 6. 최종 응답
        return new InterviewResultDetailResponse(
                session.getId(),
                session.getFinalScore(),
                "전반적으로 직무에 대한 이해도가 높습니다.", // 임시 총평
                logicAndStructure,
                speechAnalysis,
                turnScripts
        );
    }

    // --- 내부 통계 집계 로직 ---
    private SpeechAnalysis calculateSpeechAnalysis(List<InterviewRecord> records) {
        if (records == null || records.isEmpty()) {
            return new SpeechAnalysis(0, 0, 0.0f, null, Collections.emptyList());
        }

        int totalWpm = 0;
        int totalSilence = 0;
        float totalSttAccuracy = 0.0f;
        int validTurnCount = records.size();

        for (InterviewRecord record : records) {
            totalWpm += (record.getWpm() != null ? record.getWpm() : 0);
            totalSilence += (record.getSilenceCount() != null ? record.getSilenceCount() : 0);
            totalSttAccuracy += (record.getSttAccuracy() != null ? record.getSttAccuracy() : 0.0f);
        }

        int avgWpm = totalWpm / validTurnCount;
        float avgStt = totalSttAccuracy / validTurnCount;

        return new SpeechAnalysis(
                avgWpm,
                totalSilence,
                avgStt,
                null, // 감정 분석 요약 임시 처리
                Collections.emptyList()
        );
    }
}