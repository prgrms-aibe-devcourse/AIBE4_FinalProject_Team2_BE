package com.aibe.team2.domain.error.dto.admin;

import com.aibe.team2.domain.error.enums.IssueStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ErrorIssueStatusUpdateRequest {

    @NotNull
    private IssueStatus status;
}