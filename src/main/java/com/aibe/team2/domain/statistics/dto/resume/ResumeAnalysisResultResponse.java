package com.aibe.team2.domain.statistics.dto.resume;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record ResumeAnalysisResultResponse(
        Long analysisId,
        Integer totalScore, // 자소서 완성도 점수

        EvaluationSummary evaluationSummary, // 상단 4대 지표 요약
        KeywordStats keywordStats, // 상세 피드백 데이터
        List<CorrectionDetail> sentenceCorrections, // 문장별 교정 데이터
        Map<String, Object> generatedSubtitle,

        String revisedFullContent, // AI 첨삭본 전체 텍스트
        LocalDateTime analyzedAt
) {
    // 1. 상단 요약 지표 (가독성, 핵심 키워드 매칭, 논리적 설득력 등)
    public record EvaluationSummary(
            String readabilityLevel, // ex: "High"
            Integer matchedKeywordCount,
            Boolean isStarStructureApplied // STAR 기법 적용 여부
    ) {}

    // 2. 키워드 분석 데이터
    public record KeywordStats(
            List<String> goodKeywords,    // 많이 사용한 키워드
            List<String> missingKeywords  // 추천 보완 키워드
    ) {}

    // 3. 문장별 교정 데이터 (Before & After 및 개선 포인트)
    public record CorrectionDetail(
            String originalSentence,
            String correctedSentence,
            String improvementReason // 왜 이렇게 고쳤는지
    ) {}
}