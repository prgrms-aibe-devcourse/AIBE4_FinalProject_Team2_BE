package com.aibe.team2.domain.auth.filter;

import com.aibe.team2.domain.auth.dto.CustomUserDetails;
import com.aibe.team2.domain.auth.util.JwtTokenProvider;
import com.aibe.team2.domain.mypage.repository.member.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    @Value("${app.frontend.oauth-redirect-uri}")
    private String redirectUri;

    @Value("${jwt.access-token-validity:3600000}")
    private long accessTokenValidity;

    @Value("${jwt.refresh-token-validity:604800000}")
    private long refreshTokenValidity;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        // 0 - 1. 서비스(LoadUser)에서 반환한 인증 객체 꺼내기
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // 0 - 2. 이제 안전하게 데이터 추출 (null 걱정 없음!)
        String email = userDetails.getName();
        String role = userDetails.getMember().getRole().name();

        // 우리 서버의 JWT 발급
        String accessToken = jwtTokenProvider.createAccessToken(email, role);
        String refreshToken = jwtTokenProvider.createRefreshToken(email, role);

        // 1 - 1. access token 쿠키 생성
        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", accessToken)
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .maxAge(accessTokenValidity / 1000)
                .build();

        // 1 - 2. refresh token 쿠키 생성
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .maxAge(refreshTokenValidity / 1000)
                .build();

        // 2. 응답 헤더에 쿠키 추가
        response.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        // 3. 리다이렉트 (토큰 제외)
        getRedirectStrategy().sendRedirect(request, response, redirectUri);
    }
}