package com.aibe.team2.domain.error.controller;

import com.aibe.team2.domain.error.dto.admin.ErrorIssueDetailResponse;
import com.aibe.team2.domain.error.dto.admin.ErrorIssueResponse;
import com.aibe.team2.domain.error.dto.admin.ErrorIssueSearchCond;
import com.aibe.team2.domain.error.dto.admin.ErrorIssueStatusUpdateRequest;
import com.aibe.team2.domain.error.dto.admin.ErrorLogDetailResponse;
import com.aibe.team2.domain.error.dto.admin.ErrorLogRowResponse;
import com.aibe.team2.domain.error.enums.IssueStatus;
import com.aibe.team2.domain.error.service.ErrorAdminService;
import com.aibe.team2.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/issues")
public class ErrorAdminController {

    private final ErrorAdminService errorAdminService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ErrorIssueResponse>>> searchIssues(
            @ModelAttribute ErrorIssueSearchCond cond,
            Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                errorAdminService.searchIssues(cond, pageable)
        ));
    }

    @GetMapping("/{issueId}")
    public ResponseEntity<ApiResponse<ErrorIssueDetailResponse>> getIssueDetail(
            @PathVariable Long issueId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                errorAdminService.getIssueDetail(issueId)
        ));
    }

    @GetMapping("/{issueId}/logs")
    public ResponseEntity<ApiResponse<Page<ErrorLogRowResponse>>> getLogsByIssue(
            @PathVariable Long issueId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                errorAdminService.getLogsByIssue(issueId, pageable)
        ));
    }

    @GetMapping("/logs/{logId}")
    public ResponseEntity<ApiResponse<ErrorLogDetailResponse>> getLogDetail(
            @PathVariable Long logId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                errorAdminService.getLogDetail(logId)
        ));
    }

    @PatchMapping("/{issueId}/status")
    public ResponseEntity<ApiResponse<IssueStatus>> updateIssueStatus(
            @PathVariable Long issueId,
            @RequestBody @Valid ErrorIssueStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                errorAdminService.updateIssueStatus(issueId, request.getStatus())
        ));
    }
}