package com.aibe.team2.domain.mypage.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobPreferenceUpdateRequest {
    private List<String> targetJobRoles;

    private String preferredLocation;
}