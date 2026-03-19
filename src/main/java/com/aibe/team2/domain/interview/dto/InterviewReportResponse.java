package com.aibe.team2.domain.interview.dto;

import com.aibe.team2.domain.interview.entity.InterviewSession;
import com.aibe.team2.domain.interview.enums.InterviewMode;
import com.aibe.team2.domain.interview.enums.InterviewSessionStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class InterviewReportResponse {
    private Long sessionId;
    private InterviewMode interviewMode;
    private String interviewType;
    private InterviewSessionStatus status;
    private Integer finalScore;
    private LocalDateTime createdAt;

    // 연관된 이력서와 공고의 '제목'을 프론트엔드에 전달하기 위한 필드
    private String resumeTitle;
    private String jobTitle;

    // 상세 분석 결과 지표 필드
    private String overallFeedback;
    private Integer jobRelevanceScore;
    private Integer attitudeConfidenceScore;
    private Integer logicalStructureScore;
    private Integer clarityScore;
    private Integer persuasivenessScore;
    private Integer consistencyScore;

    // 개별 턴 대화 내역 (질문, 답변, 피드백)
    private List<RecordDto> records;

    public static InterviewReportResponse of(InterviewSession session, String resumeTitle, String jobTitle, List<RecordDto> records) {
        return InterviewReportResponse.builder()
                .sessionId(session.getId())
                .interviewMode(session.getInterviewMode())
                .interviewType(session.getInterviewType())
                .status(session.getStatus())
                .finalScore(session.getFinalScore())
                .createdAt(session.getCreatedAt())
                .resumeTitle(resumeTitle)
                .jobTitle(jobTitle)
                // Session 엔티티에서 분석 데이터 매핑
                .overallFeedback(session.getOverallFeedback())
                .jobRelevanceScore(session.getJobRelevanceScore())
                .attitudeConfidenceScore(session.getAttitudeConfidenceScore())
                .logicalStructureScore(session.getLogicalStructureScore())
                .clarityScore(session.getClarityScore())
                .persuasivenessScore(session.getPersuasivenessScore())
                .consistencyScore(session.getConsistencyScore())
                .records(records)
                .build();
    }

    @Getter
    @Builder
    public static class RecordDto {
        private Integer turnSequence;
        private String questionText;
        private String answerText;
        private Float evaluationScore;
        private String aiFeedback;
    }
}