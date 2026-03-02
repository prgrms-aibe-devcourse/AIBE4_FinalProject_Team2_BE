package com.aibe.team2.domain.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminCreditAdjustResponse {
    private Long memberId;
    private Integer tokenDelta;
    private Integer balanceAfter;
}