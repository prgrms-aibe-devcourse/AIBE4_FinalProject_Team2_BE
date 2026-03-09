package com.aibe.team2.domain.statistics.controller;

import com.aibe.team2.domain.auth.dto.CustomUserDetails;
import com.aibe.team2.domain.statistics.dto.interview.InterviewResultDetailResponse;
import com.aibe.team2.domain.statistics.service.InterviewStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "통계/이력 API", description = "면접 결과 통계 및 이력 조회 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mypage/interviews")
public class InterviewStatisticsController {

    private final InterviewStatisticsService interviewStatisticsService;

    // Fallback 로직: 인증 정보가 없을 시 임시 ID 반환
    private Long getMemberIdWithFallback(CustomUserDetails userDetails) {
        if(userDetails == null || userDetails.getMember() == null) {
            // TODO : 현재 개발 및 테스트 환경을 위한 Fallback ID 반환
            return 1L;
        }
        return userDetails.getMember().getMemberId();
    }

    // [FR-REP-04] 면접 이력 - 상세 조회(리포트)
    @Operation(summary = "면접 상세 결과 조회", description = "특정 면접 세션의 상세 대화 내용, 평가 점수, 피드백 등을 조회합니다. ")
    @GetMapping("/{interviewId}")
    public ResponseEntity<InterviewResultDetailResponse> getInterviewReport(
            @PathVariable("interviewId") Long interviewId,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        Long currentUserId = getMemberIdWithFallback(userDetails);

        InterviewResultDetailResponse response = interviewStatisticsService.getInterviewStatistics(interviewId, currentUserId);

        return ResponseEntity.ok(response);
    }
}
