package com.aibe.team2.domain.statistics.dto.interview;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record InterviewResultDetailResponse(
        Long sessionId,
        String InterviewType,
        String interviewMode,
        LocalDateTime createdAt,
        Integer totalScore,
        String overallFeedback,

        // [TODO] 차트 테이블 작성 시 주석 해제
        // ChartMetricsDto chartMetrics,

        LogicAndStructure logicAndStructure,
        SpeechAnalysis speechAnalysis, // 발화 습관 및 비언어적 통계
        List<TurnScript> turnScripts // 턴별 전체 대화 복기 스크립트
) {
    // 1. 답변 논리 및 구조
    public record LogicAndStructure(
            Integer clarityScore,
            Integer persuasivenessScore,
            Integer consistencyScore
    ) {}

    // 2. 비언어적 통계 집계 데이터
    public record SpeechAnalysis(
            Integer avgWpm,
            Integer totalSilenceCount,
            Float avgSttAccuracy,
            Map<String, Object> emotionSummary,
            List<String> frequentWords,
            List<String> habitDetails
    ) {}

    // 3. 대화 복기 및 개별 비언어 지표 (파라미터 10개 완벽 매칭)
    public record TurnScript(
            Integer turnSequence,
            String questionText,
            String answerText,
            String aiFeedback,
            Double evaluationScore,
            List<String> recommendedGuides,

            Integer wpm,
            Integer silenceCount,
            Float sttAccuracy,
            Map<String, Object> emotionAnalysis,
            Boolean isBookmarked
    ) {}
}