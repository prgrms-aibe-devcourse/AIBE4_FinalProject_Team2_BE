package com.aibe.team2.domain.resume.entity;

public enum AnalysisStatus {
    PENDING,      // 대기
    PROCESSING,   // 처리
    DELAYED,      // 지연
    COMPLETED,    // 완료
    FAILED,        // 실패
    CANCELLED
}