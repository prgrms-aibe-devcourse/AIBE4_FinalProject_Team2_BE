package com.aibe.team2.domain.auth.dto;

import com.aibe.team2.domain.mypage.entity.enums.Provider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberDTO {
    private String nickname;
    private String email;
    private String password;
//    private Role role;
    private Provider provider;
}
