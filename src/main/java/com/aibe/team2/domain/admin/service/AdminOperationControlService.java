package com.aibe.team2.domain.admin.service;

import com.aibe.team2.domain.admin.repository.QueueJobMetricRepository;
import com.aibe.team2.domain.resume.dto.AnalysisEvent;
import com.aibe.team2.domain.resume.entity.AnalysisStatus;
import com.aibe.team2.domain.resume.entity.AnalyzedReport;
import com.aibe.team2.domain.resume.repository.ResumeAnalysisRepository;
import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminOperationControlService {

    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final QueueJobMetricRepository queueJobMetricRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void retry(String targetType, Long targetId, String reason) {
        log.info("관리자 재처리 요청: targetType={}, targetId={}, reason={}", targetType, targetId, reason);

        if ("ANALYSIS_REPORT".equalsIgnoreCase(targetType)) {
            retryAnalysisReport(targetId);
            return;
        }

        throw new BusinessException(ErrorCode.COMMON_400);
    }

    private void retryAnalysisReport(Long reportId) {
        AnalyzedReport report = resumeAnalysisRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ANALYSIS_REPORT_NOT_FOUND));

        if (report.getStatus() != AnalysisStatus.FAILED &&
                report.getStatus() != AnalysisStatus.DELAYED) {
            throw new BusinessException(ErrorCode.COMMON_409);
        }

        int newRetryCount = queueJobMetricRepository
                .findTopByTargetTypeAndTargetIdOrderByCreatedAtDesc("ANALYSIS_REPORT", reportId)
                .map(metric -> metric.getRetryCount() + 1)
                .orElse(1);

        report.updateStatus(AnalysisStatus.PENDING);

        eventPublisher.publishEvent(
                AnalysisEvent.retry(
                        report.getId(),
                        report.getResume().getContent(),
                        newRetryCount
                )
        );
    }
}