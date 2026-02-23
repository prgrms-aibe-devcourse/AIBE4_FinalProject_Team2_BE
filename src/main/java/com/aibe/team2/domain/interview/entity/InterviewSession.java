package com.aibe.team2.domain.interview.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class) // 추가
public class InterviewSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 추가
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    private InterviewStatus status;

    private String type; // TEXT 또는 VOICE

    @Builder
    public InterviewSession(Long memberId, String type) {
        this.memberId = memberId;
        this.status = InterviewStatus.CREATED;
        this.type = type;
    }

    // 추가
    @Column(name = "final_score")
    private Integer finalScore;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public void updateStatus(InterviewStatus status) {
        this.status = status;
    }
}