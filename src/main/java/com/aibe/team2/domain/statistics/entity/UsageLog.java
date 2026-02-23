package com.aibe.team2.domain.statistics.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "usage_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UsageLog {

    @Id
    private Long id; // 읽기 전용이므로 GeneratedValue 생략 가능

    @Column(name = "member_id")
    private Long memberId;

    @Column(name = "service_type")
    private String serviceType; // 예: "RESUME", "INTERVIEW"

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}