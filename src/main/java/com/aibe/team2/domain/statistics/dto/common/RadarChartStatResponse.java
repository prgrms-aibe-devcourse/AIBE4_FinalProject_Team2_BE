package com.aibe.team2.domain.statistics.dto.common;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RadarChartStatResponse {
    private Double avgClarity;              // 평균 명확성
    private Double avgPersuasiveness;       // 평균 설득력
    private Double avgConsistency;          // 평균 일관성
    private Double jobRelevanceScore;       // 직무 적합도 점수
    private Double logicalStructureScore;   // 논리 구조 점수
    private Double attitudeConfidenceScore; // 태도 및 자신감 점수
}
