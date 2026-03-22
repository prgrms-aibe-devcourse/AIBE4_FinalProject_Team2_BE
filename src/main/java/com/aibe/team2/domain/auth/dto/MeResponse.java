package com.aibe.team2.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MeResponse {
    private String nickname;
    private String role;
    private String accessToken;
}
