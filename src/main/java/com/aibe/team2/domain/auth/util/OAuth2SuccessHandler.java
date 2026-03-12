package com.aibe.team2.domain.auth.util;

import com.aibe.team2.domain.mypage.entity.Member;
import com.aibe.team2.domain.mypage.entity.enums.Provider;
import com.aibe.team2.domain.mypage.entity.enums.Role;
import com.aibe.team2.domain.mypage.repository.member.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        // 0 - 1. 서비스(LoadUser)에서 반환한 인증 객체 꺼내기
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // 0 - 2. 이제 안전하게 데이터 추출 (null 걱정 없음!)
        String email = userDetails.getName();

        // 우리 서버의 JWT 발급
        String accessToken = jwtTokenProvider.createAccessToken(email, Role.MEMBER.name());
        String refreshToken = jwtTokenProvider.createRefreshToken(email, Role.MEMBER.name());


        // 1 - 1. access token 쿠키 생성
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .path("/")
                .httpOnly(false)
                .secure(false)      // 로컬 환경에서는 false, https 환경에서는 true
                .sameSite("Lax")   // CSRF 방어
                .maxAge(3600)      // 유효 기간 설정 - 1시간
                .build();


        // 1 - 2. refresh token 쿠키 생성
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .path("/")
                .httpOnly(false)
                .secure(false)      // 로컬 환경에서는 false, https 환경에서는 true
                .sameSite("Lax")   // CSRF 방어
                .maxAge(7 * 24 * 3600)      // 유효 기간 설정 - 일주일
                .build();

        // 2. 응답 헤더에 쿠키 추가
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        // 3. 리다이렉트 (토큰 제외)
        String targetUrl = "http://localhost:5173/AIBE4_FinalProject_Team2_FE/oauth/callback";
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}