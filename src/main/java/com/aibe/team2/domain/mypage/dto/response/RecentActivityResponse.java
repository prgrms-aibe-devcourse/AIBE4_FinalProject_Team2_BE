package com.aibe.team2.domain.mypage.dto.response;

import java.time.LocalDateTime;

public record RecentActivityResponse(
        Long id,
        String type,        // "RESUME" 또는 "INTERVIEW"
        String title,       // 자소서 제목 또는 면접 직무명
        LocalDateTime createdAt,
        Integer score       // 자소서는 null 허용, 면접은 최종 점수
) {}