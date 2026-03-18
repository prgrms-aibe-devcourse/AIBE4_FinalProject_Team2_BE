package com.aibe.team2.domain.admin.service;

import com.aibe.team2.domain.admin.dto.response.AdminOperationTargetDetailResponse;
import com.aibe.team2.domain.admin.entity.QueueJobMetric;
import com.aibe.team2.domain.admin.enums.QueueJobStatus;
import com.aibe.team2.domain.admin.repository.QueueJobMetricRepository;
import com.aibe.team2.domain.error.entity.ErrorLog;
import com.aibe.team2.domain.error.repository.ErrorLogRepository;
import com.aibe.team2.domain.mypage.entity.Member;
import com.aibe.team2.domain.mypage.repository.member.MemberRepository;
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

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminOperationControlService {

    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final QueueJobMetricRepository queueJobMetricRepository;
    private final ErrorLogRepository errorLogRepository;
    private final MemberRepository memberRepository;
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

    @Transactional
    public void cancel(String targetType, Long targetId, String reason) {
        log.info("관리자 취소 요청: targetType={}, targetId={}, reason={}", targetType, targetId, reason);

        if ("ANALYSIS_REPORT".equalsIgnoreCase(targetType)) {
            cancelAnalysisReport(targetId, reason);
            return;
        }

        throw new BusinessException(ErrorCode.COMMON_400);
    }

    private void retryAnalysisReport(Long reportId) {
        AnalyzedReport report = resumeAnalysisRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ANALYSIS_REPORT_NOT_FOUND));

        if (report.getStatus() != AnalysisStatus.FAILED
                && report.getStatus() != AnalysisStatus.DELAYED
                && report.getStatus() != AnalysisStatus.CANCELLED) {
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

    private void cancelAnalysisReport(Long reportId, String reason) {
        AnalyzedReport report = resumeAnalysisRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ANALYSIS_REPORT_NOT_FOUND));

        QueueJobMetric latestMetric = queueJobMetricRepository
                .findTopByTargetTypeAndTargetIdOrderByCreatedAtDesc("ANALYSIS_REPORT", reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_404));

        if (latestMetric.getStatus() == QueueJobStatus.PROCESSING) {
            throw new BusinessException(ErrorCode.COMMON_409);
        }

        if (latestMetric.getStatus() == QueueJobStatus.SUCCESS
                || latestMetric.getStatus() == QueueJobStatus.FAILED
                || latestMetric.getStatus() == QueueJobStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.COMMON_409);
        }

        latestMetric.markCancelled(
                reason == null || reason.isBlank() ? "관리자 취소 요청" : reason
        );

        report.updateStatus(AnalysisStatus.CANCELLED);
    }

    @Transactional(readOnly = true)
    public AdminOperationTargetDetailResponse getTargetDetail(String targetType, Long targetId) {
        if ("ANALYSIS_REPORT".equalsIgnoreCase(targetType)) {
            return getAnalysisReportDetail(targetId);
        }

        throw new BusinessException(ErrorCode.COMMON_400);
    }

    private AdminOperationTargetDetailResponse getAnalysisReportDetail(Long reportId) {
        AnalyzedReport report = resumeAnalysisRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ANALYSIS_REPORT_NOT_FOUND));

        QueueJobMetric latestMetric = queueJobMetricRepository
                .findTopByTargetTypeAndTargetIdOrderByCreatedAtDesc("ANALYSIS_REPORT", reportId)
                .orElse(null);

        ErrorLog latestErrorLog = errorLogRepository
                .findTopByTargetTypeAndTargetIdOrderByOccurredAtDesc("ANALYSIS_REPORT", reportId)
                .orElse(null);

        String latestQueueStatus = latestMetric != null ? latestMetric.getStatus().name() : null;
        Integer retryCount = latestMetric != null ? latestMetric.getRetryCount() : 0;
        String latestErrorMessage = latestErrorLog != null ? latestErrorLog.getMessage() : null;
        LocalDateTime lastProcessedAt = latestMetric != null ? latestMetric.getUpdatedAt() : null;

        Long memberId = report.getResume() != null ? report.getResume().getMemberId() : null;
        String memberEmail = null;
        String memberNickname = null;

        if (memberId != null) {
            Member member = memberRepository.findById(memberId).orElse(null);
            if (member != null) {
                memberEmail = member.getEmail();
                memberNickname = member.getNickname();
            }
        }

        boolean retryable = report.getStatus() == AnalysisStatus.FAILED
                || report.getStatus() == AnalysisStatus.DELAYED
                || report.getStatus() == AnalysisStatus.CANCELLED;

        boolean cancellable = latestMetric != null
                && latestMetric.getStatus() != QueueJobStatus.PROCESSING
                && latestMetric.getStatus() != QueueJobStatus.SUCCESS
                && latestMetric.getStatus() != QueueJobStatus.FAILED
                && latestMetric.getStatus() != QueueJobStatus.CANCELLED;

        return AdminOperationTargetDetailResponse.builder()
                .targetType("ANALYSIS_REPORT")
                .targetId(reportId)
                .currentStatus(report.getStatus().name())
                .latestQueueStatus(latestQueueStatus)
                .retryCount(retryCount)
                .latestErrorMessage(latestErrorMessage)
                .lastProcessedAt(lastProcessedAt)
                .memberId(memberId)
                .memberEmail(memberEmail)
                .memberNickname(memberNickname)
                .retryable(retryable)
                .cancellable(cancellable)
                .build();
    }
}