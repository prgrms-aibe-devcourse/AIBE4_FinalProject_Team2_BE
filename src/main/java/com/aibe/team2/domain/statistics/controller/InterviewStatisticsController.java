package com.aibe.team2.domain.statistics.controller;

import com.aibe.team2.domain.statistics.dto.interview.InterviewResultDetailResponse;
import com.aibe.team2.domain.statistics.service.InterviewStatisticsService;
import com.aibe.team2.global.common.annotation.LoginMemberId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "통계/이력 API", description = "면접 결과 통계 및 이력 조회 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mypage/interviews")
public class InterviewStatisticsController {

    private final InterviewStatisticsService interviewStatisticsService;

    // [FR-REP-04] 면접 이력 - 상세 조회(리포트)
    @Operation(summary = "면접 상세 결과 조회", description = "특정 면접 세션의 상세 대화 내용, 평가 점수, 피드백 등을 조회합니다. ")
    @GetMapping("/{interviewId}")
    public ResponseEntity<InterviewResultDetailResponse> getInterviewReport(
            @PathVariable("interviewId") Long interviewId,
            @LoginMemberId Long memberId
    ){
        InterviewResultDetailResponse response = interviewStatisticsService.getInterviewStatistics(interviewId, memberId);

        return ResponseEntity.ok(response);
    }
}
