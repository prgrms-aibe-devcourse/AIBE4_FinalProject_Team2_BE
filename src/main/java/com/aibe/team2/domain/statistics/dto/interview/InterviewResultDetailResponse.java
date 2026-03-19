package com.aibe.team2.domain.statistics.dto.interview;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 면접 결과 상세 조회 시 반환되는 최상위 DTO (Data Transfer Object)
 */
public record InterviewResultDetailResponse(
        // [기본 정보]
        Long sessionId,              // 면접 세션의 고유 식별자 (PK)
        String interviewType,        // 면접 유형 (예: "인성 면접", "기술 면접")
        String interviewMode,        // 면접 진행 방식 (예: "TEXT", "VOICE")
        LocalDateTime createdAt,     // 면접이 생성(진행)된 날짜 및 시간

        // [종합 평가]
        Integer totalScore,          // 면접 전체 종합 평가 점수 (0~100 등 지정된 척도)
        String overallFeedback,      // 면접 전체에 대한 총평 및 피드백 텍스트
        Integer jobRelevanceScore,       // 직무 적합성 점수
        Integer attitudeConfidenceScore, // 태도 및 자신감 점수

        // [TODO] 차트 테이블 작성 시 주석 해제
        // ChartMetricsDto chartMetrics,

        // [세부 분석 지표]
        LogicAndStructure logicAndStructure, // 1. 답변의 논리성 및 구조적 완성도 지표
        SpeechAnalysis speechAnalysis,       // 2. 발화 속도, 정적 등 비언어적 통계 지표
        List<TurnScript> turnScripts         // 3. 1문 1답(Turn) 단위의 상세 대화 내역 및 개별 지표
) {
    /**
     * 1. 답변 논리 및 구조 (Logic And Structure)
     * 사용자의 전체적인 답변 내용이 얼마나 체계적인지 평가하는 지표
     */
    public record LogicAndStructure(
            Integer logicalStructureScore, // 논리적 구조 종합 점수
            Integer clarityScore,        // 명확성 점수: 의도가 뚜렷하게 전달되었는가
            Integer persuasivenessScore, // 설득력 점수: 주장을 뒷받침하는 근거가 타당한가
            Integer consistencyScore     // 일관성 점수: 답변의 처음과 끝이 모순 없이 이어지는가
    ) {}

    /**
     * 2. 비언어적 통계 집계 데이터 (Speech Analysis)
     * 전체 면접 동안 발생한 음성 및 태도적 특징의 평균/합계 데이터
     */
    public record SpeechAnalysis(
            Integer avgWpm,                  // 전체 평균 말하기 속도 (Words Per Minute)
            Integer totalSilenceCount,       // 전체 대화 중 발생한 비정상적인 침묵(정적)의 총 횟수
            Float avgSttAccuracy,            // 전체 평균 음성 인식(STT) 정확도 (%)
            Map<String, Object> emotionSummary, // 전체 감정 분석 결과 요약 (예: "긴장": 40%, "평온": 60%)
            List<String> frequentWords,      // 전체 면접에서 가장 자주 사용된 키워드/단어 목록
            List<String> habitDetails        // "어..", "그.." 등 불필요한 발화 습관에 대한 상세 설명
    ) {}

    /**
     * 3. 대화 복기 및 개별 비언어 지표 (Turn Script)
     * 면접관의 질문 1개와 사용자의 답변 1개를 묶은 단일 턴에 대한 상세 데이터
     */
    public record TurnScript(
            // [기본 대화 정보]
            Long recordId,               // 해당 턴(대화 기록)의 고유 식별자 (PK)
            Integer turnSequence,        // 면접 내 대화 순서 (예: 1번째 질문, 2번째 질문)
            String questionText,         // 면접관(AI)이 질문한 내용
            String answerText,           // 지원자(사용자)가 답변한 내용

            // [해당 턴 개별 평가]
            String aiFeedback,           // 이 답변에 대한 AI의 개별 코멘트/피드백
            Double evaluationScore,      // 이 답변에 매겨진 개별 평가 점수
            List<String> recommendedGuides, // 이 질문에 대해 더 나은 답변을 하기 위한 모범 가이드라인

            // [해당 턴 개별 비언어 지표]
            Integer wpm,                 // 이 답변을 할 때의 말하기 속도 (WPM)
            Integer silenceCount,        // 이 답변 중 발생한 침묵 횟수
            Float sttAccuracy,           // 이 답변의 음성 인식(STT) 정확도 (%)
            Map<String, Object> emotionAnalysis, // 이 답변을 할 때의 감정 상태 (예: "당황함")

            // [기타 부가 기능]
            Boolean isBookmarked         // 사용자가 이 특정 질문/답변 턴을 다시 보기 위해 북마크(저장)했는지 여부
    ) {}
}