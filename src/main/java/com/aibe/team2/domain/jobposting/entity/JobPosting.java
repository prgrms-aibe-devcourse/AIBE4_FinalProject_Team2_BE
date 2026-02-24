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

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "company_name", length = 100)
    private String companyName;

    @Column(name = "job_title", nullable = false, length = 100)
    private String jobTitle;

    @Column(name = "posting_url", length = 500)
    private String postingUrl;

    @Column(name = "job_description", columnDefinition = "TEXT")
    private String jobDescription;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "jobPosting", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JobSkill> jobSkills = new ArrayList<>();

    @Builder
    public JobPosting(Long userId, String companyName, String jobTitle, String postingUrl, String jobDescription, String requiredSkills) {
        this.userId = userId;
        this.companyName = (companyName == null || companyName.isEmpty()) ? "Self-Input" : companyName;
        this.jobTitle = jobTitle;
        this.postingUrl = postingUrl;
        this.jobDescription = jobDescription;
    }

    // 연관관계 편의 메서드 추가 (공고에 스킬을 추가할 때 사용)
    // 아마 채용공고를 복사하는 형식으로 갖고오는거라 안 쓰긴 할듯.
    public void addJobSkill(JobSkill jobSkill) {
        this.jobSkills.add(jobSkill);
    }

}