package com.aibe.team2.domain.resume.entity;

import com.aibe.team2.domain.jobposting.entity.JobPosting;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode; // 🔴 추가
import org.hibernate.type.SqlTypes;          // 🔴 추가
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "analysis_report")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class AnalyzedReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_posting_id")
    private JobPosting jobPosting;

    @Enumerated(EnumType.STRING)
    @Column(name = "analysis_type", nullable = false)
    private AnalysisType analysisType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AnalysisStatus status;

    // ----- 공통 결과 필드 -----
    @Column(name = "overall_feedback", columnDefinition = "TEXT")
    private String overallFeedback;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sentence_corrections", columnDefinition = "JSON")
    private String sentenceCorrections;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "paragraph_summaries", columnDefinition = "JSON")
    private String paragraphSummaries;

    @Column(name = "revised_full_content", columnDefinition = "TEXT")
    private String revisedFullContent;

    @Column(name = "job_description", columnDefinition = "TEXT")
    private String jobDescription;

    // ----- FIT_MATCH(매칭) 전용 결과 필드 -----
    @Column(name = "match_score")
    private Integer matchScore;

    @Column(name = "matching_feedback", columnDefinition = "TEXT")
    private String matchingFeedback;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "keyword_analysis", columnDefinition = "JSON")
    private String keywordAnalysis;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "expected_questions", columnDefinition = "JSON")
    private String expectedQuestions;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public AnalyzedReport(Resume resume, AnalysisType analysisType, JobPosting jobPosting) {
        this.resume = resume;
        this.analysisType = analysisType;
        this.jobPosting = jobPosting;
        this.status = AnalysisStatus.PENDING;
    }

    public void updateStatus(AnalysisStatus status) {
        this.status = status;
    }

    public void startAnalysis() {
        this.status = AnalysisStatus.PROCESSING;
    }

    public void completeNormalAnalysis(String overallFeedback, String sentenceCorrections, String paragraphSummaries, String revisedFullContent) {
        this.overallFeedback = overallFeedback;
        this.sentenceCorrections = sentenceCorrections;
        this.paragraphSummaries = paragraphSummaries;
        this.revisedFullContent = revisedFullContent;
        this.status = AnalysisStatus.COMPLETED;
    }

    public void completeMatchAnalysis(Integer matchScore, String matchingFeedback, String keywordAnalysis,
                                      String expectedQuestions, String overallFeedback,
                                      String corrections, String paragraphSummaries, String revisedFullContent) {
        this.matchScore = matchScore;
        this.matchingFeedback = matchingFeedback;
        this.keywordAnalysis = keywordAnalysis;
        this.expectedQuestions = expectedQuestions;
        this.overallFeedback = overallFeedback;
        this.sentenceCorrections = corrections;
        this.paragraphSummaries = paragraphSummaries;
        this.revisedFullContent = revisedFullContent;
        this.status = AnalysisStatus.COMPLETED;
    }
}