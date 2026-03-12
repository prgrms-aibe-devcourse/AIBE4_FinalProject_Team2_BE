package com.aibe.team2.domain.statistics.dto.resume;

import java.time.LocalDateTime;
import java.util.List;

public record ResumeAnalysisResultResponse(
        Long analysisId,
        Integer totalScore,          // 매칭 점수 (일반 첨삭일 경우 null)

        String overallFeedback,      // 전반적인 피드백 (공통)
        String matchingFeedback,     // 채용공고 핏 피드백 (매칭 시에만 존재)

        EvaluationSummary evaluationSummary, // 상단 4대 지표 요약
        KeywordStats keywordStats,           // 상세 피드백 데이터
        List<CorrectionDetail> sentenceCorrections, // 문장별 교정 데이터

        String revisedFullContent,   // AI 첨삭본 전체 텍스트
        LocalDateTime analyzedAt
) {
    // 1. 상단 요약 지표 (가독성, 핵심 키워드 매칭, 논리적 설득력 등)
    public record EvaluationSummary(
            String readabilityLevel, // ex: "High"
            Integer matchedKeywordCount,
            Boolean isStarStructureApplied // STAR 기법 적용 여부
    ) {}

    // 2. 키워드 분석 데이터 (수정됨: good -> matched)
    public record KeywordStats(
            List<String> matchedKeywords, // 매칭된 키워드
            List<String> missingKeywords  // 추천 보완 키워드
    ) {}

    // 3. 문장별 교정 데이터 (AI 프롬프트 반환 규격에 맞춤)
    public record CorrectionDetail(
            String original,
            String corrected,
            String reason // 왜 이렇게 고쳤는지
    ) {}
}