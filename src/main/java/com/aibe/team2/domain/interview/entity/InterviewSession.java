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
    private Long id; // 세션 고유 ID

    @Column(nullable = false)
    private Long memberId; // 회원 ID

    private Long resumeId; // 이력서 ID

    private Long jobPostingId; // 채용 공고 ID

    private String interviewMode; // 면접 모드 (NORMAL, FOLLOW_UP 등)

    @Column(nullable = false)
    private String interviewType; // 면접 방식 (TEXT, VOICE)

    // AI 제공자 선택 필드 (OPEN_AI 또는 RETELL)
    private String aiProvider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewSessionStatus status; // 면접 상태 (CREATED, IN_PROGRESS 등)

    private Integer finalScore; // 최종 점수

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public InterviewSession(Long memberId, Long resumeId, Long jobPostingId, String interviewMode, String interviewType, String aiProvider) {
        this.memberId = memberId;
        this.resumeId = resumeId;
        this.jobPostingId = jobPostingId;
        this.interviewMode = interviewMode;
        this.interviewType = interviewType;
        this.aiProvider = aiProvider;
        this.status = InterviewSessionStatus.CREATED;
    }

    // 상태 변경 메서드
    public void updateStatus(InterviewSessionStatus status) {
        this.status = status;
    }
}