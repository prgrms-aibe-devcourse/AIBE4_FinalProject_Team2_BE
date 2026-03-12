package com.aibe.team2.domain.resume.service;

import com.aibe.team2.domain.resume.entity.AnalysisStatus; // (또는 ResumeAnalysisStatus)
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

    // 사용하지 않는 StatusAggregator 주석 또는 제거 (불필요한 의존성)
    // private final StatusAggregator statusAggregator;

    // 1. 에러 발생 시
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateToFailed(Long reportId) {
        // 내부 메서드에서 로그를 찍으므로 여기서는 호출만 합니다.
        updateStatusWithLog(reportId, AnalysisStatus.FAILED);
    }

    // 2. 외부 API 지연 및 타임아웃 발생 시
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateToDelayed(Long reportId) {
        updateStatusWithLog(reportId, AnalysisStatus.DELAYED);
    }

    // 3. 분석 상태를 명시적으로 변경해야 할 때 사용하는 범용 메서드 (COMPLETED 등)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void changeStatus(Long reportId, AnalysisStatus newStatus) {
        updateStatusWithLog(reportId, newStatus);
    }

    // DB 업데이트 수행 및 상태별 맞춤 로그 출력
    private void updateStatusWithLog(Long reportId, AnalysisStatus status) {
        resumeAnalysisRepository.findById(reportId).ifPresent(report -> {

            // 1. 상태 업데이트 및 저장
            report.updateStatus(status);
            resumeAnalysisRepository.save(report);

            // 2. 상태에 따른 맞춤형 가시성 로그 출력
            switch (status) {
                case COMPLETED:
                    log.info("✅ [StatusManager] 분석 성공! 리포트 ID: {} - 상태가 [COMPLETED]로 업데이트 되었습니다.", reportId);
                    break;
                case DELAYED:
                    log.warn("⏳ [StatusManager] 분석 지연! 리포트 ID: {} - 상태가 [DELAYED]로 업데이트 되었습니다.", reportId);
                    break;
                case FAILED:
                    log.error("❌ [StatusManager] 분석 실패! 리포트 ID: {} - 상태가 [FAILED]로 업데이트 되었습니다.", reportId);
                    break;
                default:
                    log.info("ℹ️ [StatusManager] 리포트 ID: {} - 상태가 [{}]로 업데이트 되었습니다.", reportId, status.name());
                    break;
            }
        });
    }
}