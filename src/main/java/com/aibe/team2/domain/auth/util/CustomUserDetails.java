package com.aibe.team2.domain.auth.util;

import com.aibe.team2.domain.mypage.entity.Member;
import org.springframework.lang.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CustomUserDetails implements UserDetails, OAuth2User {
    private Member member;
    private Map<String, Object> attributes; // 소셜 정보 담는 곳
    private String email;
    private String name;

    // 일반 로그인용 생성자
    public CustomUserDetails(Member member) {
        this.member = member;
        email = member.getEmail();
        name = member.getNickname();
    }

    // 소셜 로그인용 생성자
    public CustomUserDetails(Member member, Map<String, Object> attributes) {
        this.member = member;
        this.email = member.getEmail();
        this.name = member.getNickname();
        this.attributes = attributes;
    }

    @Nullable
    @Override
    public <A> A getAttribute(String name) {
        return OAuth2User.super.getAttribute(name);
    }

    @Override
    public Map<String, Object> getAttributes() { return attributes; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() { return member.getPassword(); }

    @Override
    public String getUsername() { return member.getEmail(); }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }


    @Override
    public String getName() { return member.getEmail(); }

    // ... 나머지 권한(getAuthorities) 등 기존 코드 유지
}