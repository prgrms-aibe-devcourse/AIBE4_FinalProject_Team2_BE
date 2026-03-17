package com.aibe.team2.domain.statistics.dto.resume;

import com.aibe.team2.domain.resume.entity.AnalyzedReport;

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
    public static ResumeAnalysisListResponse from(AnalyzedReport report){
        // 공고 정보가 있는지 확인
        boolean hasJobPosting = report.getJobPosting() != null;

        return new ResumeAnalysisListResponse(
                report.getId(),
                report.getResume().getTitle(),
                // 공고가 없으면 "자유 양식" 등의 기본 텍스트 반환
                hasJobPosting ? report.getJobPosting().getCompanyName() : "일반 첨삭(자유 양식)",
                hasJobPosting ? report.getJobPosting().getJobTitle() : "-",
                report.getMatchScore(),
                report.getStatus().name(),
                report.getCreatedAt()
        );
    }
}
