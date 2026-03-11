package com.aibe.team2.domain.mypage.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobPreferenceUpdateRequest {
    private List<String> targetJobRoles;

    private String preferredLocation;
}