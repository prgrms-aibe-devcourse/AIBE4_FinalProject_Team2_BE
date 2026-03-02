package com.aibe.team2.domain.statistics.controller;

import com.aibe.team2.domain.statistics.dto.interview.InterviewResultDetailResponse;
import com.aibe.team2.domain.statistics.service.InterviewStatisticsService;
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

    // // [FR-REP-04] 면접 이력 - 목록 조회(다건)
    // // 쿼리 파라미터로 sessionType 필터링
    // @Operation(summary = "내 면접 이력 목록 조회", description = "사용자의 전체 면접 이력과 통계 요약 정보를 리스트로 조회합니다.")
    // @GetMapping
    // public ResponseEntity<List<RadarChartStatResponse>> getInterviewList(
    //         @RequestParam(required = false) String sessionType
    //         // TODO : Spring Security 연동
    //         // @AuthenticationPrincipal CustomUserDetails userDetails
    // ){
    //     // TODO : Spring Security 연동
    //     // Long currentUserId = userDetails.getId();
    //     Long currentUserId = 1L;
    //     List<RadarChartStatResponse> response = interviewStatisticsService.getInterviewStatisticsList(currentUserId, sessionType);
    //
    //     return ResponseEntity.ok(response);
    // }

    // [FR-REP-04] 면접 이력 - 상세 조회(리포트)
    @Operation(summary = "면접 상세 결과 조회", description = "특정 면접 세션의 상세 대화 내용, 평가 점수, 피드백 등을 조회합니다. ")
    @GetMapping("/{interviewId}")
    public ResponseEntity<InterviewResultDetailResponse> getInterviewReport(
            @PathVariable("interviewId") Long interviewId
            // TODO : Spring Security 연동
            // @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        // TODO : Spring Security 연동
        // Long currentUserId = userDetails.getId();
        Long currentUserId = 1L;
        InterviewResultDetailResponse response = interviewStatisticsService.getInterviewStatistics(interviewId, currentUserId);

        return ResponseEntity.ok(response);
    }
}
