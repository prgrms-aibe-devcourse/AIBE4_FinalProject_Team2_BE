package com.aibe.team2.domain.admin.dto.response;

import com.aibe.team2.domain.mypage.entity.Member;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class AdminMemberDetailResponse {

    private final Long memberId;
    private final String email;
    private final String nickname;
    private final String role;
    private final String status;
    private final Integer creditBalance;
    private final String subscriptionPlan;
    private final String desiredJobRole;
    private final String preferredLocation;
    private final LocalDateTime createdAt;
    private final LocalDateTime deletedAt;

    public AdminMemberDetailResponse(Member member) {
        this.memberId = member.getMemberId();
        this.email = member.getEmail();
        this.nickname = member.getNickname();
        this.role = member.getRole().name();
        this.status = member.getStatus().name();
        this.creditBalance = member.getCreditBalance();
        this.subscriptionPlan = member.getSubscriptionPlan() != null
                ? member.getSubscriptionPlan().name()
                : null;
        this.desiredJobRole = member.getDesiredJobRole();
        this.preferredLocation = member.getPreferredLocation();
        this.createdAt = member.getCreatedAt();
        this.deletedAt = member.getDeletedAt();
    }
}