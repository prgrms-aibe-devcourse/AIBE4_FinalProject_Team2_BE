package com.aibe.team2.domain.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class AdminRetryRequest {

    @NotBlank
    private String targetType;

    @NotNull
    private Long targetId;

    private String reason;
}