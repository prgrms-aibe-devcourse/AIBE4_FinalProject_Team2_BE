package com.aibe.team2.domain.statistics.entity;

import com.aibe.team2.domain.interview.entity.InterviewSession;
import com.aibe.team2.global.converter.JsonAttributeConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "interview_result_statistics")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class InterviewResultStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 면접 세션과 1:1 관계 매핑(FK)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interview_session_id", nullable = false)
    private InterviewSession interviewSession;

    // 차트용 6가지 지표
    @Column(name = "avg_clarity")
    private Double avgClarity;

    @Column(name = "avg_persuasiveness")
    private Double avgPersuasiveness;

    @Column(name = "avg_consistency")
    private Double avgConsistency;

    @Column(name = "job_relevance_score")
    private Double jobRelevanceScore;

    @Column(name = "logical_structure_score")
    private Double logicalStructureScore;

    @Column(name = "attitude_confidence_score")
    private Double attitudeConfidenceScore;

    @Column(name = "overall_feedback", columnDefinition = "TEXT")
    private String overallFeedback;

    // 발화 습관 JSON 데이터
    @Convert(converter = JsonAttributeConverter.class)
    @Column(name = "speech_habits", columnDefinition = "TEXT")
    private Map<String, Object> speechHabits;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public InterviewResultStatistics(
            InterviewSession interviewSession,
            Double avgClarity,
            Double avgPersuasiveness,
            Double avgConsistency,
            Double jobRelevanceScore,
            Double logicalStructureScore,
            Double attitudeConfidenceScore,
            String overallFeedback,
            Map<String, Object> speechHabits
    ){
        this.interviewSession = interviewSession;
        this.avgClarity = avgClarity;
        this.avgPersuasiveness = avgPersuasiveness;
        this.avgConsistency = avgConsistency;
        this.jobRelevanceScore = jobRelevanceScore;
        this.logicalStructureScore = logicalStructureScore;
        this.attitudeConfidenceScore = attitudeConfidenceScore;
        this.overallFeedback = overallFeedback;
        this.speechHabits = speechHabits;
    }
}
