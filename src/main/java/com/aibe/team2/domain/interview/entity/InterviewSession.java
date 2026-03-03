package com.aibe.team2.domain.interview.entity;

import com.aibe.team2.domain.interview.enums.InterviewMode;
import com.aibe.team2.domain.interview.enums.InterviewSessionStatus;
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

    @Enumerated(EnumType.STRING) // String -> InterviewMode Enum으로 변경
    private InterviewMode interviewMode;

    @Column(nullable = false)
    private String interviewType;

    private String aiProvider;

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

    @Builder
    public InterviewSession(Long memberId, Long resumeId, Long jobPostingId, InterviewMode interviewMode, String interviewType, String aiProvider, String modelVariant, String personaType) {
        this.memberId = memberId;
        this.resumeId = resumeId;
        this.jobPostingId = jobPostingId;
        this.interviewMode = interviewMode; // Enum 타입 반영
        this.interviewType = interviewType;
        this.aiProvider = aiProvider;
        this.modelVariant = modelVariant;
        this.personaType = personaType;
        this.status = InterviewSessionStatus.CREATED;
    }

    public void updateStatus(InterviewSessionStatus status) {
        this.status = status;
    }
}