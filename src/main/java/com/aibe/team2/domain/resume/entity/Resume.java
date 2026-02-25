package com.aibe.team2.domain.resume.entity;

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
@Table(name = "resume")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "s3_file_url", length = 500)
    private String s3FileUrl;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_analyzed")
    private Boolean isAnalyzed;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public Resume(Long memberId, String title, String s3FileUrl, String content) {
        this.memberId = memberId;
        this.title = title;
        this.s3FileUrl = s3FileUrl;
        this.content = content;
        this.isAnalyzed = false;
    }

    public void updateAnalysisStatus(boolean isAnalyzed) {
        this.isAnalyzed = isAnalyzed;
    }
}