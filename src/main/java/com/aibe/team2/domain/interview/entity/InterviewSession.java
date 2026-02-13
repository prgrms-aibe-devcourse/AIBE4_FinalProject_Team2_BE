package com.aibe.team2.domain.interview.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private InterviewStatus status;

    private String type; // TEXT 또는 VOICE

    @Builder
    public InterviewSession(String type) {
        this.status = InterviewStatus.CREATED;
        this.type = type;
    }

    public void updateStatus(InterviewStatus status) {
        this.status = status;
    }
}