package com.aibe.team2.domain.admin.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminCreditAdjustRequest {

    @NotNull
    private Long memberId;

    /**
     * +면 지급, -면 차감
     */
    @NotNull
    private Integer tokenDelta;

    @Size(max = 255)
    private String reason;
}