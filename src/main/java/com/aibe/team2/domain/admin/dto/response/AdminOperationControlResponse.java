package com.aibe.team2.domain.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminOperationControlResponse {
    private String targetType;
    private Long targetId;
    private String action;
    private String result;
}