package com.aibe.team2.domain.statistics.dto.admin;

import com.aibe.team2.domain.statistics.enums.ServiceType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class UsageLogAdminRow {
    private Long id;
    private Long memberId;
    private String email;
    private ServiceType serviceType;
    private Integer amount;
    private Integer tokenUsage;
    private Integer balanceAfter;
    private String requestTraceId;
    private String targetType;
    private Long targetId;
    private String description;
    private LocalDateTime createdAt;
}