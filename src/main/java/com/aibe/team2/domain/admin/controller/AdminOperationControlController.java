package com.aibe.team2.domain.admin.controller;

import com.aibe.team2.domain.admin.dto.request.AdminCancelRequest;
import com.aibe.team2.domain.admin.dto.request.AdminRetryRequest;
import com.aibe.team2.domain.admin.dto.response.AdminOperationControlResponse;
import com.aibe.team2.domain.admin.dto.response.AdminOperationTargetDetailResponse;
import com.aibe.team2.domain.admin.service.AdminOperationControlService;
import com.aibe.team2.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<AdminOperationControlResponse>> cancel(
            @RequestBody @Valid AdminCancelRequest request
    ) {
        adminOperationControlService.cancel(
                request.getTargetType(),
                request.getTargetId(),
                request.getReason()
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        new AdminOperationControlResponse(
                                request.getTargetType(),
                                request.getTargetId(),
                                "CANCEL",
                                "ACCEPTED"
                        )
                )
        );
    }

    @GetMapping("/target")
    public ResponseEntity<ApiResponse<AdminOperationTargetDetailResponse>> getTargetDetail(
            @RequestParam String targetType,
            @RequestParam Long targetId
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        adminOperationControlService.getTargetDetail(targetType, targetId)
                )
        );
    }
}