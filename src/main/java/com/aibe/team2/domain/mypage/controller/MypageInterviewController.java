package com.aibe.team2.domain.mypage.controller;

import com.aibe.team2.domain.auth.dto.CustomUserDetails;
import com.aibe.team2.domain.mypage.dto.response.InterviewSessionListResponse;
import com.aibe.team2.domain.mypage.service.MypageInterviewService;
import com.aibe.team2.global.redis.ratelimit.RateLimit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mypage/interviews")
public class MypageInterviewController {

    private final MypageInterviewService mypageInterviewService;

    // Fallback 로직
    private Long getMemberIdWithFallback(CustomUserDetails userDetails) {
        if(userDetails == null || userDetails.getMember() == null) {
            // TODO : 개발 및 테스트 환경을 위한 Fallback ID 반환
            return 1L;
        }
        return userDetails.getMember().getMemberId();
    }

    @RateLimit
    @GetMapping
    public ResponseEntity<Page<InterviewSessionListResponse>> getInterviewSessionList(
            @AuthenticationPrincipal CustomUserDetails customUserDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long currentUserId = getMemberIdWithFallback(customUserDetails);
        Pageable pageRequest = PageRequest.of(page, size);
        Page<InterviewSessionListResponse> response = mypageInterviewService.getInterviewSessionList(currentUserId, pageRequest);

        return ResponseEntity.ok(response);
    }
}
