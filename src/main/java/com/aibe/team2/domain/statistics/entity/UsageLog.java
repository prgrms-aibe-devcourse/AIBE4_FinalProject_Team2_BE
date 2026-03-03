package com.aibe.team2.domain.statistics.entity;

import com.aibe.team2.domain.mypage.entity.Member;
import com.aibe.team2.domain.statistics.enums.ServiceType;
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
@Table(
        name = "usage_log",
        indexes = {
                @Index(name = "idx_usage_member", columnList = "member_id"),
                @Index(name = "idx_usage_created", columnList = "created_at"),
                @Index(name = "idx_usage_service", columnList = "service_type")
        }
)
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

    @Column(name = "request_trace_id", nullable = false, length = 100)
    private String requestTraceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 50)
    private ServiceType serviceType;

    @Column(name = "amount", nullable = false)
    private Integer amount;

    @Column(name = "token_usage", nullable = false)
    private Integer tokenUsage;

    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;

    @Column(name = "target_type", length = 50)
    private String targetType;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "description", length = 255)
    private String description;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    private UsageLog(
            Member member,
            String requestTraceId,
            ServiceType serviceType,
            Integer amount,
            Integer tokenUsage,
            Integer balanceAfter,
            String targetType,
            Long targetId,
            String description
    ) {
        this.member = member;
        this.requestTraceId = requestTraceId;
        this.serviceType = serviceType;
        this.amount = amount;
        this.tokenUsage = tokenUsage;
        this.balanceAfter = balanceAfter;
        this.targetType = targetType;
        this.targetId = targetId;
        this.description = description;
    }

    public static UsageLog of(
            Member member,
            String requestTraceId,
            ServiceType serviceType,
            int amount,
            int tokenUsage,
            int balanceAfter,
            String targetType,
            Long targetId,
            String description
    ) {
        return UsageLog.builder()
                .member(member)
                .requestTraceId(requestTraceId)
                .serviceType(serviceType)
                .amount(amount)
                .tokenUsage(tokenUsage)
                .balanceAfter(balanceAfter)
                .targetType(targetType)
                .targetId(targetId)
                .description(description)
                .build();
    }
}