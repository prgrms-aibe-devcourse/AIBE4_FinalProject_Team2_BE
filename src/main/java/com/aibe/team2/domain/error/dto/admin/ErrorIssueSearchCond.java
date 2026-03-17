package com.aibe.team2.domain.error.dto.admin;

import com.aibe.team2.domain.error.enums.ErrorDomain;
import com.aibe.team2.domain.error.enums.ErrorSeverity;
import com.aibe.team2.domain.error.enums.IssueStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorIssueSearchCond {

    private IssueStatus status;
    private ErrorSeverity severity;
    private ErrorDomain errorDomain;
}