package com.aibe.team2.domain.auth.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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
        String token = jwtTokenProvider.createAccessToken(email);

        // React 페이지로 리다이렉트하며 쿼리 스트링으로 토큰 전달
        // (실무에서는 쿠키를 더 권장하지만, 테스트를 위해 URL 전달 예시)
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/oauth/callback")
                .queryParam("token", token)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}