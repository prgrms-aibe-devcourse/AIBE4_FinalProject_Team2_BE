package com.aibe.team2.domain.log.controller;

import com.aibe.team2.domain.log.dto.LogDTO;
import com.aibe.team2.domain.log.service.LogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/logs")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    @GetMapping
    public ResponseEntity<List<LogDTO>> getApiLogs(
            @RequestParam(value = "date", required = false) String date) {

        // 날짜 파라미터가 없으면 오늘 날짜 기준 조회
        String targetDate = (date != null) ? date : LocalDate.now().toString();

        List<LogDTO> logs = logService.getLogsByDate(targetDate);
        return ResponseEntity.ok(logs);
    }
}