package com.aibe.team2.domain.interview.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

// Gemini AI의 JSON 응답을 매핑하기 위한 내부용 DTO (STT나 음성 엔진이 측정해야 하는 물리적 수치들은 제외됨)
@Getter
@NoArgsConstructor
public class AnalysisResultDto {
    private Integer totalScore;
    private String overallFeedback;
    private Integer jobRelevanceScore;
    private Integer attitudeConfidenceScore;

    private LogicAndStructure logicAndStructure;
    private SpeechAnalysis speechAnalysis;
    private List<RecordAnalysis> turnScripts;

    @Getter @NoArgsConstructor
    public static class LogicAndStructure {
        private Integer logicalStructureScore;
        private Integer clarityScore;
        private Integer persuasivenessScore;
        private Integer consistencyScore;
    }

    @Getter @NoArgsConstructor
    public static class SpeechAnalysis {
        // 단일 문자열이 들어와도 요소 1개짜리 List로 자동 변환하여 파싱 에러 방지
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        private List<String> frequentWords;

        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        private List<String> habitDetails;
    }

    @Getter @NoArgsConstructor
    public static class RecordAnalysis {
        private Integer turnSequence;
        private Integer evaluationScore;
        private String aiFeedback;

        // 단일 문자열 응답에 대비한 방어 로직 추가
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        private List<String> recommendedGuides;
    }
}