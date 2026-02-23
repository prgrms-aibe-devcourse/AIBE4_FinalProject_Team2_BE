package com.aibe.team2.domain.statistics.controller;

import com.aibe.team2.domain.statistics.dto.interview.InterviewResultDetailResponse;
import com.aibe.team2.domain.statistics.service.InterviewStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mypage/interview")
public class InterviewStatisticsController {

    private final InterviewStatisticsService interviewStatisticsService;

    // [FR-REP-04] 면접 이력 - 상세 조회(리포트)
    @GetMapping("/{interviewId}")
    public ResponseEntity<InterviewResultDetailResponse> getInterviewReport(
            @PathVariable("interviewId") Long interviewId
    ){
        // TODO : 하드코딩 제거
        // 임시 하드코딩된 사용자 ID
        Long currenUserId = 1L;
        InterviewResultDetailResponse response = interviewStatisticsService.getInterviewStatistics(interviewId, currenUserId);

        return ResponseEntity.ok(response);
    }
}
