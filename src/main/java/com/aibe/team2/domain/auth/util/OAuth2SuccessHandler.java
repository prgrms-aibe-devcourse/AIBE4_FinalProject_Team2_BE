package com.aibe.team2.domain.auth.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // Google의 경우 "email" 필드에 이메일이 들어있습니다.
        String email = oAuth2User.getAttribute("email");

        // 우리 서버의 JWT 발급
        String accessToken = jwtTokenProvider.createAccessToken(email);
        String refreshToken = jwtTokenProvider.createRefreshToken(email);


        // 1 - 1. access token 쿠키 생성
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", accessToken)
                .path("/")
                .httpOnly(true)    // JavaScript에서 접근 불가 (XSS 방어)
                .secure(true)      // HTTPS 환경에서만 전송
                .sameSite("Lax")   // CSRF 방어
                .maxAge(3600)      // 유효 기간 설정 - 1시간
                .build();


        // 1 - 2. refresh token 쿠키 생성
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
                .path("/")
                .httpOnly(true)    // JavaScript에서 접근 불가 (XSS 방어)
                .secure(true)      // HTTPS 환경에서만 전송
                .sameSite("Lax")   // CSRF 방어
                .maxAge(7 * 24 * 3600)      // 유효 기간 설정 - 일주일
                .build();

        // 2. 응답 헤더에 쿠키 추가
        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        // 3. 리다이렉트 (토큰 제외)
        String targetUrl = "http://localhost:5173/oauth/callback";
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}