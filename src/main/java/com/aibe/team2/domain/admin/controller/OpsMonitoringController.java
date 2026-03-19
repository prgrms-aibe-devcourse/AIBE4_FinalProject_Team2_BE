package com.aibe.team2.domain.admin.controller;

import com.aibe.team2.domain.admin.dto.ops.OpsAlertResponse;
import com.aibe.team2.domain.admin.dto.ops.OpsDashboardSummaryResponse;
import com.aibe.team2.domain.admin.dto.ops.OpsQueueHourlyResponse;
import com.aibe.team2.domain.admin.dto.ops.OpsQueueSummaryResponse;
import com.aibe.team2.domain.admin.service.OpsMonitoringService;
import com.aibe.team2.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ops")
public class OpsMonitoringController {

    private final OpsMonitoringService opsMonitoringService;

    @GetMapping("/dashboard/summary")
    public ResponseEntity<ApiResponse<OpsDashboardSummaryResponse>> getDashboardSummary() {
        return ResponseEntity.ok(
                ApiResponse.success(opsMonitoringService.getDashboardSummary())
        );
    }

    @GetMapping("/alerts")
    public ResponseEntity<ApiResponse<List<OpsAlertResponse>>> getAlerts() {
        return ResponseEntity.ok(
                ApiResponse.success(opsMonitoringService.getAlerts())
        );
    }

    @GetMapping("/queue/summary")
    public ResponseEntity<ApiResponse<OpsQueueSummaryResponse>> getQueueSummary() {
        return ResponseEntity.ok(
                ApiResponse.success(opsMonitoringService.getQueueSummary())
        );
    }

    @GetMapping("/queue/hourly")
    public ResponseEntity<ApiResponse<List<OpsQueueHourlyResponse>>> getQueueHourly(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        LocalDate targetDate = (date == null) ? LocalDate.now() : date;

        return ResponseEntity.ok(
                ApiResponse.success(opsMonitoringService.getQueueHourly(targetDate))
        );
    }
}