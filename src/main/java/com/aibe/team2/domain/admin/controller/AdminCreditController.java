package com.aibe.team2.domain.admin.controller;

import com.aibe.team2.domain.admin.dto.request.AdminCreditAdjustRequest;
import com.aibe.team2.domain.admin.dto.response.AdminCreditAdjustResponse;
import com.aibe.team2.domain.admin.service.AdminCreditService;
import com.aibe.team2.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/credits")
public class AdminCreditController {

    private final AdminCreditService adminCreditService;

    @PostMapping("/adjust")
    public ResponseEntity<ApiResponse<AdminCreditAdjustResponse>> adjustCredit(
            @RequestBody @Valid AdminCreditAdjustRequest request
    ) {
        int after = adminCreditService.adjustCreditByAdmin(
                request.getMemberId(),
                request.getTokenDelta(),
                request.getReason()
        );

        AdminCreditAdjustResponse response = new AdminCreditAdjustResponse(
                request.getMemberId(),
                request.getTokenDelta(),
                after
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}