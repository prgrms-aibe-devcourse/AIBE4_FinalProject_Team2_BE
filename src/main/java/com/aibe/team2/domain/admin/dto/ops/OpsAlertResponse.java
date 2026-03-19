package com.aibe.team2.domain.admin.dto.ops;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class OpsAlertResponse {
    private String alertType;
    private String severity;
    private String message;
    private String targetType;
    private Long targetId;
}