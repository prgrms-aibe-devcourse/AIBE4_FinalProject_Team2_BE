package com.aibe.team2.domain.admin.dto.request;

import com.aibe.team2.domain.mypage.entity.enums.MemberStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class AdminMemberStatusUpdateRequest {
    @NotNull
    private MemberStatus status;
}