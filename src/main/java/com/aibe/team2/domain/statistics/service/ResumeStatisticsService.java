package com.aibe.team2.domain.statistics.service;

import com.aibe.team2.domain.resume.entity.ResumeAnalysisReport;
import com.aibe.team2.domain.resume.entity.ResumeAnalysisStatus;
import com.aibe.team2.domain.resume.repository.ResumeAnalysisRepository;
import com.aibe.team2.domain.statistics.dto.resume.ResumeAnalysisResultResponse;
import com.aibe.team2.domain.statistics.dto.resume.ResumeAnalysisResultResponse.CorrectionDetail;
import com.aibe.team2.domain.statistics.dto.resume.ResumeAnalysisResultResponse.EvaluationSummary;
import com.aibe.team2.domain.statistics.dto.resume.ResumeAnalysisResultResponse.KeywordStats;
import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.custom.ForbiddenException;
import com.aibe.team2.global.exception.custom.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeStatisticsService {

    private final ResumeAnalysisRepository resumeAnalysisRepository;

    // [FR-REP-04] 자기소개서 첨삭 이력 - 상세 조회
    @Transactional(readOnly = true)
    public ResumeAnalysisResultResponse getResumeAnalysisReport(Long analysisId, Long currentUserId) {

        // 1. 분석 리포트 단건 조회
        ResumeAnalysisReport report = resumeAnalysisRepository.findById(analysisId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COMMON_404));

        // 2. 권한 검증 (내 자기소개서가 맞는지 확인)
        // Resume 엔티티가 memberId 필드를 가지고 있다고 가정
        Long resumeOwnerId = report.getResume().getMemberId();

        if (!resumeOwnerId.equals(currentUserId)) {
            throw new ForbiddenException(ErrorCode.COMMON_403);
        }

        // 3. 비즈니스 룰 처리 (분석 중인지 확인)
        boolean isProcessing = report.getStatus() == ResumeAnalysisStatus.PROCESSING;

        // 4. JSON 데이터 파싱 (안전한 메서드 사용)
        // 분석 중일 때는 빈 리스트 반환, 완료되면 DB JSON 파싱
        List<String> goodKeywords = isProcessing ? Collections.emptyList() : extractList(report.getKeywordAnalysis(), "goodKeywords");
        List<String> missingKeywords = isProcessing ? Collections.emptyList() : extractList(report.getKeywordAnalysis(), "missingKeywords");
        List<CorrectionDetail> corrections = isProcessing ? Collections.emptyList() : extractCorrections(report.getSentenceCorrection());

        // 4-1. 상단 요약 지표
        EvaluationSummary summary = null;
        if(!isProcessing){
            summary = new EvaluationSummary(
                    "High", // TODO: 실제 점수 기반 등급 산정 로직 필요 시 수정
                    goodKeywords.size(),
                    true
            );
        }

        // 4-2. 키워드 통계
        KeywordStats keywordStats = isProcessing ? null : new KeywordStats(goodKeywords, missingKeywords);

        // 5. 최종 DTO 반환
        return new ResumeAnalysisResultResponse(
                report.getId(),
                isProcessing ? null : report.getMatchScore(),
                summary,
                keywordStats,
                corrections,
                isProcessing ? null : report.getGeneratedSubtitle(),
                isProcessing ? null : report.getRevisedFullContent(),
                report.getCreatedAt()
        );
    }

    // --- 내부 매핑 로직 (Private Methods) ---

    /**
     * Map에서 List<String>을 안전하게 추출하는 공통 메서드
     * (기존 extractGoodKeywords, extractMissingKeywords 통합)
     */
    private List<String> extractList(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key)) {
            return Collections.emptyList();
        }

        Object value = map.get(key);
        if (!(value instanceof List<?>)) {
            return Collections.emptyList();
        }

        // 안전한 형변환: 요소 하나하나를 String으로 변환
        return ((List<?>) value).stream()
                .map(String::valueOf)
                .toList();
    }

    /**
     * Map에서 첨삭 상세 내용(CorrectionDetail)을 추출
     */
    @SuppressWarnings("unchecked")
    private List<CorrectionDetail> extractCorrections(Map<String, Object> correctionMap) {
        if (correctionMap == null || !correctionMap.containsKey("corrections")) {
            return Collections.emptyList();
        }

        Object correctionsObj = correctionMap.get("corrections");
        if (!(correctionsObj instanceof List<?>)) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> list = (List<Map<String, Object>>) correctionsObj;

        return list.stream()
                .map(c -> new CorrectionDetail(
                        String.valueOf(c.getOrDefault("originalSentence", "")),
                        String.valueOf(c.getOrDefault("correctedSentence", "")),
                        String.valueOf(c.getOrDefault("reason", ""))
                ))
                .toList();
    }
}