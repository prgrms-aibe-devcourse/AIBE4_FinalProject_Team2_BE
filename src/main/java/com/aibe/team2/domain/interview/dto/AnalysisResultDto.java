package com.aibe.team2.domain.interview.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

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
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        private List<String> frequentWords;

        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        private List<String> habitDetails;
    }

    @Getter @NoArgsConstructor
    public static class RecordAnalysis {
        private Integer turnSequence;

        // [추가] 점수 부여 전 AI가 4단계 사고 과정을 적을 필드
        private String evaluationReason;

        private Integer evaluationScore;
        private String aiFeedback;

        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        private List<String> recommendedGuides;
    }
}