package com.aibe.team2.domain.statistics.controller;

import com.aibe.team2.domain.statistics.dto.admin.DailyUsageAdminRow;
import com.aibe.team2.domain.statistics.dto.admin.UsageLogAdminRow;
import com.aibe.team2.domain.statistics.dto.admin.UsageLogAdminSearchCond;
import com.aibe.team2.domain.statistics.enums.ServiceType;
import com.aibe.team2.domain.statistics.service.AdminUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/usage")
public class AdminUsageController {

    private final AdminUsageService adminUsageService;

    @GetMapping("/daily")
    public ResponseEntity<List<DailyUsageAdminRow>> getDailyUsage(
            @RequestParam("date") LocalDate date
    ) {
        return ResponseEntity.ok(adminUsageService.getDailyUsage(date));
    }

    @GetMapping("/logs")
    public ResponseEntity<Page<UsageLogAdminRow>> searchUsageLogs(
            @ModelAttribute UsageLogAdminSearchCond cond,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(adminUsageService.searchUsageLogs(cond, pageable));
    }
}