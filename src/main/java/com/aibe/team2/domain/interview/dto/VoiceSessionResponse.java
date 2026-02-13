package com.aibe.team2.domain.interview.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VoiceSessionResponse {
    private String accessToken; // Retell AI 접속용 토큰
}