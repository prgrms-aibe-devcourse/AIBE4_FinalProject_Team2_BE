package com.aibe.team2.domain.mypage.dto.response;

import com.aibe.team2.domain.mypage.entity.Member;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class MemberUpdateResponseDto {
    private final Long userId;
    private final String email;
    private final String nickname;
    private final String profileImageUrl;
    private final MemberResponseDto.JobPreferencesDto jobPreferences;
    private final LocalDateTime updatedAt;

    public MemberUpdateResponseDto(Member member) {
        this.userId = member.getMemberId();
        this.email = member.getEmail();
        this.nickname = member.getNickname();
        this.profileImageUrl = member.getProfileImageUrl();

        this.jobPreferences = new MemberResponseDto.JobPreferencesDto(member);
        this.updatedAt = member.getUpdatedAt();
    }
}