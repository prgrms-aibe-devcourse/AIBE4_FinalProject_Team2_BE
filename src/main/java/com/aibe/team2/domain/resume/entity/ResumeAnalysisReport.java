package com.aibe.team2.domain.resume.entity;

import com.aibe.team2.domain.jobposting.entity.JobPosting;
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
    // ⭐ 객체 참조(@ManyToOne)로 변경
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_posting_id", nullable = false)
    private JobPosting jobPostingId;

    @Column(name = "match_score")
    private Integer matchScore;

    // JSON 타입 (MySQL 등에서 JSON 컬럼 사용 시 columnDefinition 명시 권장)
    // ⭐ Convert 어노테이션 추가 및 Map<String, Object> 변환
    @Convert(converter = JsonAttributeConverter.class)
    @Column(name = "keyword_analysis", columnDefinition = "TEXT")
    private Map<String, Object> keywordAnalysis;

    // ⭐ Convert 어노테이션 추가 및 Map<String, Object> 변환
    @Convert(converter = JsonAttributeConverter.class)
    @Column(name = "sentence_correction", columnDefinition = "TEXT")
    private Map<String, Object> sentenceCorrection;

    // New! 이미지 반영: JSON 타입
    // ⭐ Convert 어노테이션 추가 및 Map<String, Object> 변환
    @Convert(converter = JsonAttributeConverter.class)
    @Column(name = "generated_subtitle", columnDefinition = "TEXT")
    private Map<String, Object> generatedSubtitle;

    @Column(name = "revised_full_content", columnDefinition = "TEXT")
    private String revisedFullContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ResumeAnalysisStatus status; // DEFAULT 'PENDING'

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public ResumeAnalysisReport(Resume resume, JobPosting jobPostingId) {
        this.resume = resume;
        this.jobPostingId = jobPostingId;
        this.status = ResumeAnalysisStatus.PENDING;
    }

    // 분석
    public void startAnalysis() {
        this.status = ResumeAnalysisStatus.PROCESSING;
    }

    // 분석 완료
    public void completeAnalysis(Integer matchScore, Map<String, Object> generatedSubtitle, Map<String, Object> keywordAnalysis, Map<String, Object> sentenceCorrection, String revisedFullContent) {
        this.matchScore = matchScore;
        this.generatedSubtitle = generatedSubtitle;
        this.keywordAnalysis = keywordAnalysis;
        this.sentenceCorrection = sentenceCorrection;
        this.revisedFullContent = revisedFullContent;
        this.status = ResumeAnalysisStatus.COMPLETED;
    }

    public void failAnalysis() {
        this.status = ResumeAnalysisStatus.FAILED;
    }

    public void updateStatus(ResumeAnalysisStatus resumeAnalysisStatus) {
        this.status = resumeAnalysisStatus;
    }

}