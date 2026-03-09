package com.aibe.team2.domain.admin.controller;

import com.aibe.team2.domain.admin.dto.response.AdminDashboardSummaryResponse;
import com.aibe.team2.domain.admin.service.AdminDashboardService;
import com.aibe.team2.domain.statistics.dto.admin.UsageLogAdminRow;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/summary")
    public ResponseEntity<AdminDashboardSummaryResponse> getSummary() {
        return ResponseEntity.ok(adminDashboardService.getSummary());
    }

    @GetMapping("/recent-logs")
    public ResponseEntity<List<UsageLogAdminRow>> getRecentLogs() {
        return ResponseEntity.ok(adminDashboardService.getRecentLogs());
    }
}