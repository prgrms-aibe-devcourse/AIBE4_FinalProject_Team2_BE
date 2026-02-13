package com.aibe.team2.domain.resume.entity;

import com.aibe.team2.global.common.constant.AnalysisStatus;
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
@Table(
        name = "resume_analysis_report",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "unique_analysis",
                        columnNames = {"resume_id", "job_posting_id"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ResumeAnalysisReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK (resume.id), NOT NULL
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    // FK (job_posting.id), NOT NULL
    @Column(name = "job_posting_id", nullable = false)
    private Long jobPostingId;

    @Column(name = "match_score")
    private Integer matchScore;

    // JSON 타입 (MySQL 등에서 JSON 컬럼 사용 시 columnDefinition 명시 권장)
    @Column(name = "keyword_analysis", columnDefinition = "json")
    private String keywordAnalysis;

    @Column(name = "sentence_correction", columnDefinition = "json")
    private String sentenceCorrection;

    // New! 이미지 반영: JSON 타입
    @Column(name = "generated_subtitle", columnDefinition = "json")
    private String generatedSubtitle;

    @Column(name = "revised_full_content", columnDefinition = "TEXT")
    private String revisedFullContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AnalysisStatus status; // DEFAULT 'PENDING'

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public ResumeAnalysisReport(Resume resume, Long jobPostingId) {
        this.resume = resume;
        this.jobPostingId = jobPostingId;
        this.status = AnalysisStatus.PENDING;
    }


    // 분석
    public void startAnalysis() {
        this.status = AnalysisStatus.PROCESSING;
    }

    public void completeAnalysis(Integer matchScore, String generatedSubtitle, String keywordAnalysis, String sentenceCorrection, String revisedFullContent) {
        this.matchScore = matchScore;
        this.generatedSubtitle = generatedSubtitle;
        this.keywordAnalysis = keywordAnalysis;
        this.sentenceCorrection = sentenceCorrection;
        this.revisedFullContent = revisedFullContent;
        this.status = AnalysisStatus.COMPLETED;
    }

    public void failAnalysis() {
        this.status = AnalysisStatus.FAILED;
    }
}