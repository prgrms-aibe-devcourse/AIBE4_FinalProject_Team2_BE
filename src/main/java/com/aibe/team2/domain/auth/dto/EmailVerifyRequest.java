package com.aibe.team2.domain.auth.dto;

import lombok.Getter;

@Getter
public class EmailVerifyRequest {
    private String email;
    private String code;
}
