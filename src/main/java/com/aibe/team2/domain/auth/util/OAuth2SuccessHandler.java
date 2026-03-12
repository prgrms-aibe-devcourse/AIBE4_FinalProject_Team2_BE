package com.aibe.team2.domain.auth.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername(); // 또는 설정하신 이메일 필드 getter

        // 우리 서버의 JWT 발급
        String accessToken = jwtTokenProvider.createAccessToken(email);
        String refreshToken = jwtTokenProvider.createRefreshToken(email);


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