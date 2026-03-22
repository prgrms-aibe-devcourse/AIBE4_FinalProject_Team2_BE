package com.aibe.team2.domain.error.controller;

import com.aibe.team2.domain.error.dto.admin.ErrorIssueStatusUpdateRequest;
import com.aibe.team2.domain.error.service.ErrorIssueService;
import com.aibe.team2.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/error-issues")
public class ErrorIssueAdminController {

    private final ErrorIssueService errorIssueService;

    @PatchMapping("/{issueId}/status")
    public ResponseEntity<ApiResponse<Void>> updateIssueStatus(
            @PathVariable Long issueId,
            @RequestBody @Valid ErrorIssueStatusUpdateRequest request
    ) {
        errorIssueService.updateIssueStatus(issueId, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success());
    }
}