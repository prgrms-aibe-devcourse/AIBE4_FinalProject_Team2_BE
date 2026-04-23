package com.aibe.team2.domain.interview.entity;

import com.aibe.team2.domain.interview.enums.InterviewMode;
import com.aibe.team2.domain.interview.enums.InterviewSessionStatus;
import com.aibe.team2.domain.interview.enums.JobRole;
import com.aibe.team2.domain.interview.enums.ExperienceLevel;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import com.aibe.team2.domain.interview.dto.AnalysisResultDto;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class InterviewSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    private Long resumeId;

    private Long jobPostingId;

    @Column(columnDefinition = "TEXT")
    private String jobDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewMode interviewMode;

    @Column(nullable = false)
    private String interviewType;

    private String aiProvider;

    private String modelVariant;

    // [수정] String -> Enum 타입으로 변경 및 @Enumerated 적용
    @Enumerated(EnumType.STRING)
    @Column(name = "job_role")
    private JobRole jobRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "experience")
    private ExperienceLevel experience;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewSessionStatus status;

    private Integer finalScore;

    @Column(columnDefinition = "TEXT")
    private String overallFeedback;

    private Integer jobRelevanceScore;
    private Integer attitudeConfidenceScore;
    private Integer logicalStructureScore;
    private Integer clarityScore;
    private Integer persuasivenessScore;
    private Integer consistencyScore;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // [수정] Builder 파라미터도 String 대신 Enum 타입으로 변경
    @Builder
    public InterviewSession(Long memberId, Long resumeId, Long jobPostingId, String jobDescription, InterviewMode interviewMode, String interviewType, String aiProvider, String modelVariant, JobRole jobRole, ExperienceLevel experience) {
        this.memberId = memberId;
        this.resumeId = resumeId;
        this.jobPostingId = jobPostingId;
        this.jobDescription = jobDescription;
        this.interviewMode = interviewMode;
        this.interviewType = interviewType;
        this.aiProvider = aiProvider;
        this.modelVariant = modelVariant;
        this.jobRole = jobRole;           // Enum 할당
        this.experience = experience;     // Enum 할당
        this.status = InterviewSessionStatus.CREATED;
    }

    public void updateStatus(InterviewSessionStatus status) {
        this.status = status;
    }

    // AI 분석 결과인 최종 점수를 업데이트
    public void updateFinalScore(Integer finalScore) {
        this.finalScore = finalScore;
    }

    // [PR 리뷰 반영] AI 분석 완료 시 DTO를 통째로 전달받아 7개의 지표와 총평을 업데이트
    public void updateAnalysisResult(AnalysisResultDto analysisResult) {
        if (analysisResult == null) return;

        this.finalScore = analysisResult.getTotalScore();
        this.overallFeedback = analysisResult.getOverallFeedback();
        this.jobRelevanceScore = analysisResult.getJobRelevanceScore();
        this.attitudeConfidenceScore = analysisResult.getAttitudeConfidenceScore();

        AnalysisResultDto.LogicAndStructure logic = analysisResult.getLogicAndStructure();
        if (logic != null) {
            this.logicalStructureScore = logic.getLogicalStructureScore();
            this.clarityScore = logic.getClarityScore();
            this.persuasivenessScore = logic.getPersuasivenessScore();
            this.consistencyScore = logic.getConsistencyScore();
        } else {
            this.logicalStructureScore = 0;
            this.clarityScore = 0;
            this.persuasivenessScore = 0;
            this.consistencyScore = 0;
        }
    }
}