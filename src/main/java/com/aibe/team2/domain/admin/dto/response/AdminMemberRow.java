package com.aibe.team2.domain.admin.dto.response;

import com.aibe.team2.domain.mypage.entity.enums.MemberStatus;
import com.aibe.team2.domain.mypage.entity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AdminMemberRow {
    private Long memberId;
    private String email;
    private String nickname;
    private Role role;
    private MemberStatus status;
    private Integer creditBalance;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;
}