package com.aibe.team2.domain.interview.entity;

import com.aibe.team2.domain.interview.enums.InterviewMode;
import com.aibe.team2.domain.interview.enums.InterviewSessionStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewMode interviewMode;

    @Column(nullable = false)
    private String interviewType;

    private String aiProvider;

    private String modelVariant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewSessionStatus status;

    private Integer finalScore;

    // 상세 분석 결과 저장을 위한 필드들 추가
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

    @Builder
    public InterviewSession(Long memberId, Long resumeId, Long jobPostingId, InterviewMode interviewMode, String interviewType, String aiProvider, String modelVariant) {
        this.memberId = memberId;
        this.resumeId = resumeId;
        this.jobPostingId = jobPostingId;
        this.interviewMode = interviewMode;
        this.interviewType = interviewType;
        this.aiProvider = aiProvider;
        this.modelVariant = modelVariant;
        this.status = InterviewSessionStatus.CREATED;
    }

    public void updateStatus(InterviewSessionStatus status) {
        this.status = status;
    }

    //AI 분석 결과인 최종 점수를 업데이트
    public void updateFinalScore(Integer finalScore) {
        this.finalScore = finalScore;
    }

    //AI 분석 완료 시 7개의 지표와 총평을 한 번에 업데이트하는 메서드
    public void updateAnalysisResult(
            Integer finalScore, String overallFeedback, Integer jobRelevanceScore,
            Integer attitudeConfidenceScore, Integer logicalStructureScore,
            Integer clarityScore, Integer persuasivenessScore, Integer consistencyScore
    ) {
        this.finalScore = finalScore;
        this.overallFeedback = overallFeedback;
        this.jobRelevanceScore = jobRelevanceScore;
        this.attitudeConfidenceScore = attitudeConfidenceScore;
        this.logicalStructureScore = logicalStructureScore;
        this.clarityScore = clarityScore;
        this.persuasivenessScore = persuasivenessScore;
        this.consistencyScore = consistencyScore;
    }
}