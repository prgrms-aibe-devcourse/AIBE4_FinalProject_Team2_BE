package com.aibe.team2.domain.error.controller;

import com.aibe.team2.domain.error.dto.admin.ErrorLogDetailResponse;
import com.aibe.team2.domain.error.service.ErrorAdminService;
import com.aibe.team2.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/logs")
public class ErrorAdminLogController {

    private final ErrorAdminService errorAdminService;

    @GetMapping("/{logId}")
    public ResponseEntity<ApiResponse<ErrorLogDetailResponse>> getLogDetail(
            @PathVariable Long logId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                errorAdminService.getLogDetail(logId)
        ));
    }
}