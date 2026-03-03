package com.aibe.team2.domain.mypage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResumeUpdateResponse {
    // 프론트엔드에서 다음 동작을 수행할 수 있도록 수정된 자기소개서 ID 반환
    private Long resumeId;
}
