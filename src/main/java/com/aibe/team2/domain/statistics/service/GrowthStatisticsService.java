package com.aibe.team2.domain.statistics.service;

import com.aibe.team2.domain.interview.repository.InterviewSessionRepository;
import com.aibe.team2.domain.resume.repository.ResumeAnalysisRepository;
import com.aibe.team2.domain.statistics.dto.GrowthResultResponse;
import com.aibe.team2.domain.statistics.repository.interview.InterviewResultStatisticsRepository;
import com.aibe.team2.domain.statistics.util.GrowthCalculator;
import com.querydsl.core.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static com.aibe.team2.domain.statistics.entity.QInterviewResultStatistics.interviewResultStatistics;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GrowthStatisticsService {

    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final InterviewSessionRepository interviewSessionRepository;
    private final InterviewResultStatisticsRepository interviewResultStatisticsRepository;

    public List<GrowthResultResponse> getGrowthStatistics(Long memberId) {
        List<GrowthResultResponse> growthResults = new ArrayList<>();

        // 1. 기간 설정 - 이번 달과 지난달의 시작/종료 시간 계산
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime currentMonthStart = YearMonth.now().atDay(1).atStartOfDay();
        LocalDateTime previousMonthStart = YearMonth.now().minusMonths(1).atDay(1).atStartOfDay();
        LocalDateTime previousMonthEnd = currentMonthStart.minusNanos(1);

        // 2. 이력서 수정 횟수 집계 - 제공된 쿼리 메서드 활용
        long currResumeEdits = resumeAnalysisRepository.countByMemberIdAndCreatedAtBetween(
                memberId, currentMonthStart, now);
        long prevResumeEdits = resumeAnalysisRepository.countByMemberIdAndCreatedAtBetween(
                memberId, previousMonthStart, previousMonthEnd);
        growthResults.add(GrowthCalculator.calculateGrowth(
                "이력서 수정 횟수",
                BigDecimal.valueOf(prevResumeEdits),
                BigDecimal.valueOf(currResumeEdits)
        ));

        // 3. 모의 면접 횟수 집계
        long currInterviews = interviewSessionRepository.countByMemberIdAndCreatedAtBetween(
                memberId, currentMonthStart, now);
        long prevInterviews = interviewSessionRepository.countByMemberIdAndCreatedAtBetween(
                memberId, previousMonthStart, previousMonthEnd);

        growthResults.add(GrowthCalculator.calculateGrowth(
                "모의 면접 횟수",
                BigDecimal.valueOf(prevInterviews),
                BigDecimal.valueOf(currInterviews)
        ));

        // 4. 답변 정확도 평균 집계
        Tuple currTuple = interviewResultStatisticsRepository.findAverageMetricsTupleByMemberIdAndCreatedAtBetween(
                memberId, currentMonthStart, now);
        Tuple prevTuple = interviewResultStatisticsRepository.findAverageMetricsTupleByMemberIdAndCreatedAtBetween(
                memberId, previousMonthStart, previousMonthEnd);

        // 명확성 점수
        growthResults.add(GrowthCalculator.calculateGrowth("명확성 점수",
                getSafeScore(prevTuple, interviewResultStatistics.avgClarity.avg()),
                getSafeScore(currTuple, interviewResultStatistics.avgClarity.avg())));

        // 설득력 점수
        growthResults.add(GrowthCalculator.calculateGrowth("설득력 점수",
                getSafeScore(prevTuple, interviewResultStatistics.avgPersuasiveness.avg()),
                getSafeScore(currTuple, interviewResultStatistics.avgPersuasiveness.avg())));

        // 일관성 점수
        growthResults.add(GrowthCalculator.calculateGrowth("일관성 점수",
                getSafeScore(prevTuple, interviewResultStatistics.avgConsistency.avg()),
                getSafeScore(currTuple, interviewResultStatistics.avgConsistency.avg())));

        // 직무 적합성 점수
        growthResults.add(GrowthCalculator.calculateGrowth("직무 적합성 점수",
                getSafeScore(prevTuple, interviewResultStatistics.jobRelevanceScore.avg()),
                getSafeScore(currTuple, interviewResultStatistics.jobRelevanceScore.avg())));

        // 논리적 구조 점수
        growthResults.add(GrowthCalculator.calculateGrowth("논리적 구조 점수",
                getSafeScore(prevTuple, interviewResultStatistics.logicalStructureScore.avg()),
                getSafeScore(currTuple, interviewResultStatistics.logicalStructureScore.avg())));

        // 태도 및 자신감 점수
        growthResults.add(GrowthCalculator.calculateGrowth("태도 및 자신감 점수",
                getSafeScore(prevTuple, interviewResultStatistics.attitudeConfidenceScore.avg()),
                getSafeScore(currTuple, interviewResultStatistics.attitudeConfidenceScore.avg())));

        return growthResults;
    }

    // 튜플 전용 헬퍼 메서드
    private BigDecimal getSafeScore(Tuple tuple, com.querydsl.core.types.dsl.NumberExpression<Double> expression) {
        if (tuple == null || tuple.get(expression) == null) {
            // 값이 아예 없을 때도 "0.00"으로 포맷을 예쁘게 맞춰줍니다.
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        // DB에서 꺼낸 Double 값을 BigDecimal로 바꾼 직후, 바로 소수점 둘째 자리에서 반올림!
        return BigDecimal.valueOf(tuple.get(expression))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
