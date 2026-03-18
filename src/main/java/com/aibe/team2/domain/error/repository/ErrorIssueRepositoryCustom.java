package com.aibe.team2.domain.error.repository;

import com.aibe.team2.domain.error.dto.admin.ErrorIssueSearchCond;
import com.aibe.team2.domain.error.entity.ErrorIssue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ErrorIssueRepositoryCustom {
    Page<ErrorIssue> search(ErrorIssueSearchCond cond, Pageable pageable);
}