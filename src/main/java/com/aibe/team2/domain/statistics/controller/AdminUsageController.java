package com.aibe.team2.domain.statistics.controller;

import com.aibe.team2.domain.statistics.dto.admin.DailyUsageAdminRow;
import com.aibe.team2.domain.statistics.service.AdminUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/usage")
public class AdminUsageController {

    private final AdminUsageService adminUsageService;

    // 예: GET /api/v1/admin/usage/daily?date=2026-03-02
    @GetMapping("/daily")
    public ResponseEntity<List<DailyUsageAdminRow>> getDailyUsage(
            @RequestParam("date") LocalDate date
    ) {
        return ResponseEntity.ok(adminUsageService.getDailyUsage(date));
    }
}