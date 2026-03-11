package com.aibe.team2.domain.mypage.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProfileUpdate {
    private String nickname;
    private String profileImageUrl; // 제거 시 null 또는 빈 값 전송
    private JobPreferenceUpdateRequest jobPreferences;
}
