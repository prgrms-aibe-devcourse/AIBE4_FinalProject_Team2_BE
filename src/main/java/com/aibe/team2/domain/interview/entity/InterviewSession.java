package com.aibe.team2.domain.interview.entity;

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

    private String interviewMode;

    @Column(nullable = false)
    private String interviewType;

    private String aiProvider;

    // 신규 필드 추가
    private String modelVariant;
    private String personaType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewSessionStatus status;

    private Integer finalScore;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder // 생성자 파라미터를 업데이트하여 빌더가 인식하게 함
    public InterviewSession(Long memberId, Long resumeId, Long jobPostingId, String interviewMode, String interviewType, String aiProvider, String modelVariant, String personaType) {
        this.memberId = memberId;
        this.resumeId = resumeId;
        this.jobPostingId = jobPostingId;
        this.interviewMode = interviewMode;
        this.interviewType = interviewType;
        this.aiProvider = aiProvider;
        this.modelVariant = modelVariant; // 추가
        this.personaType = personaType;   // 추가
        this.status = InterviewSessionStatus.CREATED;
    }

    public void updateStatus(InterviewSessionStatus status) {
        this.status = status;
    }
}