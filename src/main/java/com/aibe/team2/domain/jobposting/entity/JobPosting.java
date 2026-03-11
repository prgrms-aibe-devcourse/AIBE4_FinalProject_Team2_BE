package com.aibe.team2.domain.jobposting.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "job_posting")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "company_name", length = 100)
    private String companyName;

    @Column(name = "job_title", nullable = false, length = 100)
    private String jobTitle;

    @Column(name = "posting_url", length = 500)
    private String postingUrl;

    @Column(name = "job_description", columnDefinition = "TEXT")
    private String jobDescription;

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Column(name = "embedding", columnDefinition = "vector(768)")
    private float[] embedding;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "jobPosting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobSkill> jobSkills = new ArrayList<>();

    @Builder
    public JobPosting(Long memberId, String companyName, String jobTitle, String postingUrl, String jobDescription, float[] embedding) {
        this.memberId = memberId;
        this.companyName = (companyName == null || companyName.isEmpty()) ? "Self-Input" : companyName;
        this.jobTitle = jobTitle;
        this.postingUrl = postingUrl;
        this.jobDescription = jobDescription;
        this.embedding = embedding; // 벡터 데이터 초기화 추가
    }

    public void addJobSkill(JobSkill jobSkill) {
        this.jobSkills.add(jobSkill);
    }
}