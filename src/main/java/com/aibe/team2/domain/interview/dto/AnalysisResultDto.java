package com.aibe.team2.domain.interview.dto;

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
        private List<String> frequentWords;
        private List<String> habitDetails;
    }

    @Getter @NoArgsConstructor
    public static class RecordAnalysis {
        private Integer turnSequence;
        private Integer evaluationScore;
        private String aiFeedback;
        private List<String> recommendedGuides;
    }
}