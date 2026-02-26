package com.aibe.team2.domain.statistics.service;

import com.aibe.team2.domain.resume.entity.ResumeAnalysisReport;
import com.aibe.team2.domain.resume.repository.ResumeAnalysisRepository;
import com.aibe.team2.domain.statistics.dto.resume.ResumeAnalysisResultResponse;
import com.aibe.team2.domain.statistics.dto.resume.ResumeAnalysisResultResponse.CorrectionDetail;
import com.aibe.team2.domain.statistics.dto.resume.ResumeAnalysisResultResponse.EvaluationSummary;
import com.aibe.team2.domain.statistics.dto.resume.ResumeAnalysisResultResponse.KeywordStats;
import com.aibe.team2.domain.resume.entity.ResumeAnalysisStatus;
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
// TODO : 하드코딩 부분 수정
public class ResumeStatisticsService {

    private final ResumeAnalysisRepository resumeAnalysisRepository;

    // [FR-REP-04] 자기소개서 첨삭 이력 - 상세 조회
    @Transactional(readOnly = true)
    public ResumeAnalysisResultResponse getResumeAnalysisReport(Long analysisId, Long currentUserId) {

        // 1. 분석 리포트 단건 조회(연관된 이력서 함께 조회 가정)
        ResumeAnalysisReport report = resumeAnalysisRepository.findById(analysisId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COMMON_404));

        // 2. 권한 검증
        if(!report.getResume().getMemberId().equals(currentUserId)){
            throw new ForbiddenException(ErrorCode.COMMON_403);
        }

        // 3. 비즈니스 룰 처리
        boolean isProcessing = report.getStatus() == ResumeAnalysisStatus.PROCESSING;

        // 4. JSON 데이터 파싱 및 내부 Record 조립
        List<String> goodKeywords = isProcessing ? Collections.emptyList() : extractGoodKeywords(report.getKeywordAnalysis());
        List<String> missingKeywords = isProcessing ? Collections.emptyList() : extractMissingKeywords(report.getKeywordAnalysis());
        List<CorrectionDetail> corrections = isProcessing ? Collections.emptyList() : extractCorrections(report.getSentenceCorrection());

        // 4-1. 상단 요약 지표
        EvaluationSummary summary = null;
        if(!isProcessing){
            summary = new EvaluationSummary(
                    "High",
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
                isProcessing ? null : report.getRevisedFullContent(), // After: 첨삭 완료된 자소서 본문
                report.getCreatedAt()
        );
    }

    // 내부 매핑 로직

    // @SuppressWarnings("unchecked")
    // private List<String> extractGoodKeywords(Map<String, Object> keywordMap){
    //     if(keywordMap == null || !keywordMap.containsKey("keywords")){
    //         return Collections.emptyList();
    //     }
    //     List<Map<String, Object>> list = (List<Map<String, Object>>) keywordMap.get("keywords");
    //     return list.stream().map(k -> String.valueOf(k.get("keyword"))).toList();
    // }
    //
    // @SuppressWarnings("unchecked")
    // private List<String> extractMissingKeywords(Map<String, Object> keywordMap){
    //     // 실제 JSON 구조에 "missingKeywords"키가 있을 경우를 대비한 로직
    //     if(keywordMap != null && keywordMap.containsKey("missingKeywords")){
    //         List<Map<String, Object>> missingList = (List<Map<String, Object>>) keywordMap.get("missingKeywords");
    //         return missingList.stream().map(k -> String.valueOf(k.get("keyword"))).toList();
    //     }
    //     return List.of("대규모 트래픽", "시스템 설계"); // JSON 구조 확정 전 임시 데이터
    // }
    //
    // @SuppressWarnings("unchecked")
    // private List<CorrectionDetail> extractCorrections(Map<String, Object> correctionMap){
    //     if(correctionMap == null || !correctionMap.containsKey("corrections")){
    //         return Collections.emptyList();
    //     }
    //
    //     List<Map<String, String>> list = (List<Map<String, String>>) correctionMap.get("corrections");
    //     return list.stream()
    //             .map(c -> new CorrectionDetail(
    //                     c.get("original"),
    //                     c.get("corrected"),
    //                     c.get("reason")
    //             )).toList();
    // }

    // --- 내부 매핑 로직 ---

    @SuppressWarnings("unchecked")
    private List<String> extractGoodKeywords(Map<String, Object> keywordMap){
        // DB JSON 구조: {"goodKeywords": ["분석력", "꾸준함"], ...}
        if(keywordMap == null || !keywordMap.containsKey("goodKeywords")){
            return Collections.emptyList();
        }
        // Map 객체가 아니라 순수 String 리스트이므로 바로 변환 후 반환
        return (List<String>) keywordMap.get("goodKeywords");
    }

    @SuppressWarnings("unchecked")
    private List<String> extractMissingKeywords(Map<String, Object> keywordMap){
        // DB JSON 구조: {..., "missingKeywords": ["대규모 트래픽", "시스템 설계"]}
        if(keywordMap == null || !keywordMap.containsKey("missingKeywords")){
            return Collections.emptyList();
        }
        // Map 객체가 아니라 순수 String 리스트이므로 바로 변환 후 반환
        return (List<String>) keywordMap.get("missingKeywords");
    }

    @SuppressWarnings("unchecked")
    private List<CorrectionDetail> extractCorrections(Map<String, Object> correctionMap){
        if(correctionMap == null || !correctionMap.containsKey("corrections")){
            return Collections.emptyList();
        }

        List<Map<String, Object>> list = (List<Map<String, Object>>) correctionMap.get("corrections");
        return list.stream()
                .map(c -> new CorrectionDetail(
                        // DB에 넣은 키값(originalSentence, correctedSentence)과 정확히 매칭
                        String.valueOf(c.getOrDefault("originalSentence", "")),
                        String.valueOf(c.getOrDefault("correctedSentence", "")),
                        String.valueOf(c.getOrDefault("reason", ""))
                )).toList();
    }
}
