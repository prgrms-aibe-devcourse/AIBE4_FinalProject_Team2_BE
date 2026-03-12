package com.aibe.team2.domain.resume.entity;

import com.aibe.team2.domain.jobposting.entity.JobPosting;
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
@Table(name = "analysis_report") // ERD 테이블명에 맞춤
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

    // 일반 첨삭일 때는 채용 공고가 없으므로 Null 허용
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

    @Column(name = "sentence_corrections", columnDefinition = "JSON")
    private String sentenceCorrections;

    @Column(name = "revised_full_content", columnDefinition = "TEXT")
    private String revisedFullContent;

    // FIT_MATCH(매칭) 시에만 들어오는 공고 원본 텍스트
    @Column(name = "job_description", columnDefinition = "TEXT")
    private String jobDescription;

    // ----- FIT_MATCH(매칭) 전용 결과 필드 -----
    @Column(name = "match_score")
    private Integer matchScore;

    @Column(name = "matching_feedback", columnDefinition = "TEXT")
    private String matchingFeedback;

    @Column(name = "keyword_analysis", columnDefinition = "JSON")
    private String keywordAnalysis;

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

    // 분석
    public void startAnalysis() {
        this.status = AnalysisStatus.PROCESSING;
    }

    // 1. 일반 첨삭 결과 업데이트
    public void completeNormalAnalysis(String overallFeedback, String sentenceCorrections, String revisedFullContent) {
        this.overallFeedback = overallFeedback;
        this.sentenceCorrections = sentenceCorrections;
        this.revisedFullContent = revisedFullContent;
        this.status = AnalysisStatus.COMPLETED;
    }

    // 2. 공고 매칭 결과 업데이트
    public void completeMatchAnalysis(Integer matchScore, String matchingFeedback, String keywordAnalysis,
                                      String overallFeedback, String corrections, String revisedFullContent) {
        this.matchScore = matchScore;
        this.matchingFeedback = matchingFeedback;
        this.keywordAnalysis = keywordAnalysis;
        this.overallFeedback = overallFeedback;
        this.sentenceCorrections = corrections;
        this.revisedFullContent = revisedFullContent;
        this.status = AnalysisStatus.COMPLETED;
    }
}