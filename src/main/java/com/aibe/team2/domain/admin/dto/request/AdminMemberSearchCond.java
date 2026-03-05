package com.aibe.team2.domain.admin.dto.request;

import com.aibe.team2.domain.mypage.entity.enums.MemberStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminMemberSearchCond {
    private Long memberId;
    private String email;
    private String nickname;
    private MemberStatus status;
}