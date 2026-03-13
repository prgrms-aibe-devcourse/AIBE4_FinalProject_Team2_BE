package com.aibe.team2.domain.statistics.service;

import com.aibe.team2.domain.resume.entity.AnalysisStatus;
import com.aibe.team2.domain.resume.entity.AnalyzedReport;
import com.aibe.team2.domain.resume.repository.ResumeAnalysisRepository;
import com.aibe.team2.domain.statistics.dto.resume.ResumeAnalysisListResponse;
import com.aibe.team2.domain.statistics.dto.resume.ResumeAnalysisResultResponse;
import com.aibe.team2.domain.statistics.dto.resume.ResumeAnalysisResultResponse.CorrectionDetail;
import com.aibe.team2.domain.statistics.dto.resume.ResumeAnalysisResultResponse.EvaluationSummary;
import com.aibe.team2.domain.statistics.dto.resume.ResumeAnalysisResultResponse.KeywordStats;
import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.BusinessException;
import com.aibe.team2.global.exception.custom.ForbiddenException;
import com.aibe.team2.global.exception.custom.NotFoundException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeStatisticsService {

    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final ObjectMapper objectMapper;

    // [FR-REP-04] 자기소개서 첨삭 이력 조회
    public Page<ResumeAnalysisListResponse> getResumeAnalysisList(long memberId, Pageable pageable) {
        return resumeAnalysisRepository.findByMemberIdWithDetails(memberId, pageable)
                .map(ResumeAnalysisListResponse::from);
    }

    // [FR-REP-04] 자기소개서 첨삭 이력 - 상세 조회
    @Cacheable(
            cacheNames = "resumeReport",
            key = "#analysisId + ':' + #currentUserId",
            unless = "#result.totalScore() == null && #result.overallFeedback() == null"
    )
    @Transactional(readOnly = true)
    public ResumeAnalysisResultResponse getResumeAnalysisReport(Long analysisId, Long currentUserId) {

        // 1. 분석 리포트 단건 조회
        AnalyzedReport report = resumeAnalysisRepository.findById(analysisId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COMMON_404));

        // 2. 권한 검증
        Long resumeOwnerId = report.getResume().getMemberId();
        if (!resumeOwnerId.equals(currentUserId)) {
            throw new ForbiddenException(ErrorCode.COMMON_403);
        }

        boolean isProcessing = report.getStatus() == AnalysisStatus.PROCESSING || report.getStatus() == AnalysisStatus.PENDING;

        // 3. JSON 데이터 파싱 (문자열 -> 객체)
        KeywordStats keywordStats = null;
        if (!isProcessing && report.getKeywordAnalysis() != null) {
            try {
                JsonNode kwNode = objectMapper.readTree(report.getKeywordAnalysis());
                List<String> matched = objectMapper.convertValue(kwNode.path("matchedKeywords"), new TypeReference<>() {});
                List<String> missing = objectMapper.convertValue(kwNode.path("missingKeywords"), new TypeReference<>() {});

                keywordStats = new KeywordStats(
                        matched == null ? Collections.emptyList() : matched,
                        missing == null ? Collections.emptyList() : missing
                );
            } catch (Exception e) {
                log.warn("[ResumeStatisticsService] 키워드 분석 JSON 파싱 에러 - reportId: {}", report.getId(), e);
                keywordStats = new KeywordStats(Collections.emptyList(), Collections.emptyList());
            }
        }

        List<CorrectionDetail> sentenceCorrections = Collections.emptyList();
        if (!isProcessing && report.getSentenceCorrections() != null) {
            try {
                // 저장된 JSON 배열 문자열을 List<CorrectionDetail>로 바로 파싱
                sentenceCorrections = objectMapper.readValue(report.getSentenceCorrections(), new TypeReference<>() {});
            } catch (Exception e) {
                log.warn("[ResumeStatisticsService] 교정 내역 JSON 파싱 에러 - reportId: {}", report.getId(), e);
            }
        }

        // 4. 상단 요약 지표 산정
        EvaluationSummary summary = null;
        if (!isProcessing) {
            int matchedCount = (keywordStats != null && keywordStats.matchedKeywords() != null) ? keywordStats.matchedKeywords().size() : 0;
            summary = new EvaluationSummary(
                    "High", // TODO: 실제 점수 기반 등급 산정 필요 시 수정
                    matchedCount,
                    true
            );
        }
        // [FR-RES-06] COMPLETED가 상태가 아니면 조회 불가
        if (report.getStatus() != AnalysisStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.COMMON_400); // 아직 분석이 완료되지 않음
        }
        // 5. 최종 DTO 반환
        return new ResumeAnalysisResultResponse(
                report.getId(),
                isProcessing ? null : report.getMatchScore(),
                isProcessing ? null : report.getOverallFeedback(),
                isProcessing ? null : report.getMatchingFeedback(),
                summary,
                keywordStats,
                sentenceCorrections,
                isProcessing ? null : report.getRevisedFullContent(),
                report.getCreatedAt()
        );
    }
}