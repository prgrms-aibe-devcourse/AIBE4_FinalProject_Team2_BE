package com.aibe.team2.domain.statistics.entity;

import com.aibe.team2.domain.mypage.entity.Member; // Member 엔티티 import 필요
import com.aibe.team2.domain.statistics.enums.ServiceType; // [1] 방금 만든 Enum import
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "usage_log")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class UsageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false)
    private ServiceType serviceType;

    @Column(name = "amount")
    private Integer amount;

    @Column(name = "token_usage")
    private Integer tokenUsage;

    @Column(name = "balance_after")
    private Integer balanceAfter;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public UsageLog(Member member, ServiceType serviceType, Integer amount, Integer tokenUsage, Integer balanceAfter, LocalDateTime createdAt) {
        this.member = member;
        this.serviceType = serviceType;
        this.amount = amount;
        this.tokenUsage = tokenUsage;
        this.balanceAfter = balanceAfter;
        this.createdAt = createdAt;
    }
}