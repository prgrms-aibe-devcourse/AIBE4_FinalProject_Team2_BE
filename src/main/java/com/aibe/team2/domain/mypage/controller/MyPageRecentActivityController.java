package com.aibe.team2.domain.mypage.controller;

import com.aibe.team2.domain.mypage.service.RecentActivityService;
import com.aibe.team2.domain.mypage.dto.response.RecentActivityResponse;
import com.aibe.team2.global.common.annotation.LoginMemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mypage") // /statistics가 빠진 깔끔한 경로
@RequiredArgsConstructor
public class MyPageRecentActivityController {

    private final RecentActivityService recentActivityService;

    // GET /api/v1/mypage/recent-activities
    @GetMapping("/recent-activities")
    public ResponseEntity<List<RecentActivityResponse>> getRecentActivities(
            @LoginMemberId Long memberId
    ) {
        List<RecentActivityResponse> response = recentActivityService.getRecentActivities(memberId);
        return ResponseEntity.ok(response);
    }
}
