package com.aibe.team2.domain.statistics.service;

import com.aibe.team2.domain.interview.entity.InterviewSession;
import com.aibe.team2.domain.interview.enums.InterviewMode;
import com.aibe.team2.domain.interview.repository.InterviewRepository;
import com.aibe.team2.domain.statistics.dto.common.RadarChartStatResponse;
import com.aibe.team2.domain.statistics.dto.interview.InterviewResultDetailResponse;
import com.aibe.team2.domain.statistics.dto.interview.InterviewResultDetailResponse.LogicAndStructure;
import com.aibe.team2.domain.statistics.dto.interview.InterviewResultDetailResponse.SpeechAnalysis;
import com.aibe.team2.domain.statistics.dto.interview.InterviewResultDetailResponse.TurnScript;
import com.aibe.team2.domain.statistics.entity.InterviewRecord;
import com.aibe.team2.domain.statistics.entity.InterviewResultStatistics;
import com.aibe.team2.domain.statistics.repository.interview.InterviewRecordRepository;
import com.aibe.team2.domain.statistics.repository.interview.InterviewResultStatisticsRepository;
import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.custom.ForbiddenException;
import com.aibe.team2.global.exception.custom.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewStatisticsService {

    private final InterviewRepository interviewRepository;
    private final InterviewRecordRepository recordRepository;
    private final InterviewResultStatisticsRepository statisticsRepository;

    // [단건 상세 조회]
    @Cacheable(cacheNames = "interviewDetail", key = "#sessionId + ':' + #currentMemberId ")
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
                        record.getId(),                  // 💡 [추가] 누락된 Long 타입 ID (메서드명은 엔티티에 맞게 확인 필요)
                        record.getTurnSequence(),
                        record.getQuestionText(),
                        record.getAnswerText(),
                        record.getFeedbackText(),
                        record.getEvaluationScore() != null ? record.getEvaluationScore().doubleValue() : 0.0,
                        Collections.<String>emptyList(), // 💡 [수정] List<String>으로 명시적 타입 추론 지시
                        record.getWpm(),
                        record.getSilenceCount(),
                        record.getSttAccuracy(),
                        formatEmotionMap(record.getEmotionAnalysis()),
                        false
                ))
                .toList();

        // 8. 최종 응답
        String overallReview = (stats != null && stats.getOverallFeedback() != null)
                ? stats.getOverallFeedback() : "";

        String interviewType = session.getInterviewType();
        String interviewMode = convertInterviewModeToKorean(session.getInterviewMode());

        return new InterviewResultDetailResponse(
                session.getId(),
                interviewType,
                interviewMode,
                session.getCreatedAt(),
                session.getFinalScore(),
                overallReview,
                logicAndStructure,
                speechAnalysis,
                turnScripts
        );
    }

    // [다건 목록 동적 조회]
    @Cacheable(cacheNames = "interviewList", key = "#currentMemberId + ':' + #sessionType")
    @Transactional(readOnly = true)
    public List<RadarChartStatResponse> getInterviewStatisticsList(Long currentMemberId, String sessionType) {

        List<InterviewResultStatistics> statsList = statisticsRepository.findStatisticsByCondition(currentMemberId, sessionType);
        return statsList.stream()
                .map(stat -> RadarChartStatResponse.builder() // 💡 빌더 패턴 사용
                        .avgClarity(formatScore(stat.getAvgClarity()))
                        .avgPersuasiveness(formatScore(stat.getAvgPersuasiveness()))
                        .avgConsistency(formatScore(stat.getAvgConsistency()))
                        .jobRelevanceScore(formatScore(stat.getJobRelevanceScore()))
                        .logicalStructureScore(formatScore(stat.getLogicalStructureScore()))
                        .attitudeConfidenceScore(formatScore(stat.getAttitudeConfidenceScore()))
                        .build())
                .toList();
    }

    // --- 내부 통계 집계 로직 ---
    private SpeechAnalysis calculateSpeechAnalysis(List<InterviewRecord> records) {
        if (records == null || records.isEmpty()) {
            return new SpeechAnalysis(0, 0, 0.0f, Collections.emptyMap(), Collections.emptyList(), Collections.emptyList());
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

        int avgWpm = validTurnCount > 0 ? totalWpm / validTurnCount : 0;
        float rawAvgStt = validTurnCount > 0 ? totalSttAccuracy / validTurnCount : 0.0f;
        float avgStt = (float) (Math.round(rawAvgStt * 100.0) / 100.0);

        Map<String, Object> emotionMap = records.get(0).getEmotionAnalysis();
        Map<String, Object> formattedEmotionMap = formatEmotionMap(emotionMap);

        return new SpeechAnalysis(
                avgWpm,
                totalSilence,
                avgStt,
                formattedEmotionMap,
                Collections.emptyList(), // [추가] frequentWords (많이 사용한 전문 용어 - 현재는 임시로 빈 리스트 반환)
                Collections.emptyList()  // habitDetails (개선이 필요한 발화 습관 - 현재는 임시로 빈 리스트 반환)
        );
    }

    private String convertInterviewModeToKorean(InterviewMode mode) {
        if (mode == null) {
            return "일반"; // 기본값
        }
        return switch (mode) {
            case NORMAL -> "일반";
            case FOLLOW_UP -> "심층 꼬리질문";
            case STRESS -> "압박";
        };
    }

    // 소수점 첫째 자리 반올림 메서드
    private Double formatScore(Double score){
        if(score == null) return 0.0;
        return Math.round(score*10) / 10.0;
    }

    // 감정 분석 Map 값 반올림(소수점 둘째 자리)
    private Map<String, Object> formatEmotionMap(Map<String, Object> emotionMap) {
        if (emotionMap == null || emotionMap.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Object> formattedMap = new HashMap<>();

        // 맵의 모든 키-값을 하나씩 꺼내서 확인
        for (Map.Entry<String, Object> entry : emotionMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // 만약 값이 숫자(Double, Float 등)라면?
            if (value instanceof Number) {
                double doubleVal = ((Number) value).doubleValue();
                // 소수점 둘째 자리까지 반올림 (예: 0.9267 -> 0.93)
                double roundedVal = Math.round(doubleVal * 100.0) / 100.0;
                formattedMap.put(key, roundedVal);
            } else {
                // 숫자가 아니면 그냥 원래 값 넣기
                formattedMap.put(key, value);
            }
        }
        return formattedMap;
    }
}