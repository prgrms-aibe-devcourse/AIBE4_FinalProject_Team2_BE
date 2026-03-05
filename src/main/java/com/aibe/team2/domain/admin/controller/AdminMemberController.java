package com.aibe.team2.domain.admin.controller;

import com.aibe.team2.domain.admin.dto.request.AdminMemberSearchCond;
import com.aibe.team2.domain.admin.dto.request.AdminMemberStatusUpdateRequest;
import com.aibe.team2.domain.admin.dto.response.AdminMemberRow;
import com.aibe.team2.domain.admin.dto.response.AdminMemberStatusUpdateResponse;
import com.aibe.team2.domain.admin.service.AdminMemberService;
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
@RequestMapping("/api/v1/admin/members")
public class AdminMemberController {

    private final AdminMemberService adminMemberService;

    // GET /api/v1/admin/members?email=...&status=ACTIVE&page=0&size=20
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AdminMemberRow>>> searchMembers(
            @ModelAttribute AdminMemberSearchCond cond,
            Pageable pageable
    ) {
        Page<AdminMemberRow> result = adminMemberService.searchMembers(cond, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // PATCH /api/v1/admin/members/{memberId}/status
    @PatchMapping("/{memberId}/status")
    public ResponseEntity<ApiResponse<AdminMemberStatusUpdateResponse>> updateStatus(
            @PathVariable Long memberId,
            @RequestBody @Valid AdminMemberStatusUpdateRequest request
    ) {
        var updated = adminMemberService.updateMemberStatus(memberId, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success(new AdminMemberStatusUpdateResponse(memberId, updated)));
    }
}