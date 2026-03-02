package com.aibe.team2.domain.statistics.dto.resume;

import com.aibe.team2.domain.resume.entity.ResumeAnalysisReport;

import java.time.LocalDateTime;

public record ResumeAnalysisListResponse(
        Long analysisId, // 클릭 시 상세 조회(리포트)로 넘어가기 위한 키값
        String resumeTitle, // 자기소개서 제목
        String companyName, // 지원 회사명
        String jobTitle, // 지원 직무명
        Integer matchScore, // 매칭 점수
        String status, // 분석 상태(COMPLETED, PROCESSING 등)
        LocalDateTime createdAt // 분석 요청 일시
) {
    public static ResumeAnalysisListResponse from(ResumeAnalysisReport report){
        return new ResumeAnalysisListResponse(
                report.getId(),
                report.getResume().getTitle(),               // 연관된 이력서에서 제목 추출
                report.getJobPosting().getCompanyName(),     // 연관된 공고에서 회사명 추출
                report.getJobPosting().getJobTitle(),        // 연관된 공고에서 직무 추출
                report.getMatchScore(),
                report.getStatus().name(),
                report.getCreatedAt()
        );
    }
}
