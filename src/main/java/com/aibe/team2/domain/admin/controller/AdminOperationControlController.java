package com.aibe.team2.domain.admin.controller;

import com.aibe.team2.domain.admin.dto.request.AdminRetryRequest;
import com.aibe.team2.domain.admin.dto.response.AdminOperationControlResponse;
import com.aibe.team2.domain.admin.service.AdminOperationControlService;
import com.aibe.team2.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/operations")
public class AdminOperationControlController {

    private final AdminOperationControlService adminOperationControlService;

    @PostMapping("/retry")
    public ResponseEntity<ApiResponse<AdminOperationControlResponse>> retry(
            @RequestBody @Valid AdminRetryRequest request
    ) {
        adminOperationControlService.retry(
                request.getTargetType(),
                request.getTargetId(),
                request.getReason()
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        new AdminOperationControlResponse(
                                request.getTargetType(),
                                request.getTargetId(),
                                "RETRY",
                                "ACCEPTED"
                        )
                )
        );
    }
}