package com.aibe.team2.domain.jobposting.entity;

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
@Table(name = "job_posting")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "company_name", length = 100)
    private String companyName;

    @Column(name = "job_title", nullable = false, length = 100)
    private String jobTitle;

    // [New] 원본 채용 공고 URL
    @Column(name = "posting_url", length = 500)
    private String postingUrl;

    @Column(name = "job_description", columnDefinition = "TEXT")
    private String jobDescription;

    // DB는 JSON, Java는 String
    @Column(name = "required_skills", columnDefinition = "json")
    private String requiredSkills;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    public JobPosting(Long userId, String companyName, String jobTitle, String postingUrl, String jobDescription, String requiredSkills) {
        this.userId = userId;
        this.companyName = (companyName == null || companyName.isEmpty()) ? "Self-Input" : companyName;
        this.jobTitle = jobTitle;
        this.postingUrl = postingUrl;
        this.jobDescription = jobDescription;
        this.requiredSkills = requiredSkills;
    }
}