package com.aibe.team2.domain.auth.dto;

import com.aibe.team2.domain.mypage.entity.enums.Provider;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberDTO {
    private String nickname;
    private String email;
    private String password;
//    private Role role;
    private Provider provider;
}
