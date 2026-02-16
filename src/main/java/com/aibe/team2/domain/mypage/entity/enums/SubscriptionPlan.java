package com.aibe.team2.domain.mypage.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SubscriptionPlan {

    FREE("무료 플랜"),
    PRO("프로 플랜"),
    ENTERPRISE("엔터프라이즈 플랜");

    private final String description;
}
