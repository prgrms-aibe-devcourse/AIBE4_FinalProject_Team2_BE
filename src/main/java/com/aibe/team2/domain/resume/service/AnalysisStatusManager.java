package com.aibe.team2.domain.resume.service;

import com.aibe.team2.domain.resume.entity.AnalysisStatus;
import com.aibe.team2.domain.resume.repository.ResumeAnalysisRepository;
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

    // 1. 에러 발생 시
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateToFailed(Long reportId) {
        updateStatusWithLog(reportId, AnalysisStatus.FAILED);
    }

    // 2. 외부 API 지연 및 타임아웃 발생 시
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateToDelayed(Long reportId) {
        updateStatusWithLog(reportId, AnalysisStatus.DELAYED);
    }

    // 3. 분석 상태를 명시적으로 변경해야 할 때 사용하는 범용 메서드
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void changeStatus(Long reportId, AnalysisStatus newStatus) {
        updateStatusWithLog(reportId, newStatus);
    }

    // DB 업데이트 수행
    private void updateStatusWithLog(Long reportId, AnalysisStatus status) {
        resumeAnalysisRepository.findById(reportId).ifPresent(report -> {
            report.updateStatus(status);
            log.info("[StatusManager] 리포트 ID: {} - 상태가 [{}]로 안전하게 업데이트 되었습니다.", reportId, status.name());
        });
    }
}