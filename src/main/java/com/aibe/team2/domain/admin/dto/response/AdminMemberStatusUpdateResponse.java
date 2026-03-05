package com.aibe.team2.domain.admin.dto.response;

import com.aibe.team2.domain.mypage.entity.enums.MemberStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminMemberStatusUpdateResponse {
    private Long memberId;
    private MemberStatus status;
}