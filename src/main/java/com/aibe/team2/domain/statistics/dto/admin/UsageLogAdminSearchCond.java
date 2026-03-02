package com.aibe.team2.domain.statistics.dto.admin;

import com.aibe.team2.domain.statistics.enums.ServiceType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class UsageLogAdminSearchCond {
    private Long memberId;
    private ServiceType serviceType;
    private LocalDate from;
    private LocalDate to;
    private String targetType;
}