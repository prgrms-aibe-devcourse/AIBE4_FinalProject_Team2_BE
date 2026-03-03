package com.aibe.team2.domain.mypage.dto.response;

import com.aibe.team2.domain.mypage.entity.Member;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MemberUpdateResponse {
    private final Long userId;
    private final String email;
    private final String nickname;
    private final String profileImageUrl;
    private final MemberResponse.JobPreferencesDto jobPreferences;
    private final LocalDateTime updatedAt;

    public MemberUpdateResponse(Member member) {
        this.userId = member.getMemberId();
        this.email = member.getEmail();
        this.nickname = member.getNickname();
        this.profileImageUrl = member.getProfileImageUrl();

        this.jobPreferences = new MemberResponse.JobPreferencesDto(member);
        this.updatedAt = member.getUpdatedAt();
    }
}