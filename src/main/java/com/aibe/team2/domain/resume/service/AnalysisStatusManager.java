package com.aibe.team2.domain.resume.service;

import com.aibe.team2.domain.resume.entity.ResumeAnalysisReport;
import com.aibe.team2.domain.resume.entity.ResumeAnalysisStatus;
import com.aibe.team2.domain.resume.repository.ResumeAnalysisRepository;
import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisStatusManager {

    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final ResumeAnalysisRepository analysisRepository;

    // 비동기 스레드나 큐 컨슈머에서 호출되므로, 항상 새로운 트랜잭션(REQUIRES_NEW)으로 상태를 즉시 DB에 반영하는 것이 안전
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStatus(Long reportId, ResumeAnalysisStatus status) {
        ResumeAnalysisReport report = analysisRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ANALYSIS_REPORT_NOT_FOUND));

        report.updateStatus(status); // 엔티티에 updateStatus 메서드 필요
        log.info("Report ID: {} status updated to {}", reportId, status);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failAnalysis(Long reportId, String errorMessage) {
        ResumeAnalysisReport report = analysisRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ANALYSIS_REPORT_NOT_FOUND));

        report.updateStatus(ResumeAnalysisStatus.FAILED);
        // 필요하다면 에러 메시지를 저장하는 컬럼을 추가해 기록
        log.error("Report ID: {} analysis failed. Reason: {}", reportId, errorMessage);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStatusToFailed(Long reportId) {
        resumeAnalysisRepository.findById(reportId).ifPresent(report -> {
            report.failAnalysis();
            log.error("[StatusManager] 리포트 ID: {} - 분석 실패 상태(FAILED)로 안전하게 업데이트 되었습니다.", reportId);
        });
    }

}