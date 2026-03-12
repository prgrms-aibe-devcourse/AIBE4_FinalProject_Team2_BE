package com.aibe.team2.domain.auth.util;

import com.aibe.team2.domain.mypage.entity.Member;
import com.aibe.team2.domain.mypage.entity.enums.Provider;
import com.aibe.team2.domain.mypage.entity.enums.Role;
import com.aibe.team2.domain.mypage.repository.member.MemberRepository;
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
    private final MemberRepository memberRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String nickname = oAuth2User.getAttribute("name");

        Member member = memberRepository.findByEmail(email)
                .orElseGet(() -> memberRepository.save(
                        new Member(
                                email,
                                null,
                                (nickname != null && !nickname.isBlank()) ? nickname : email.split("@")[0],
                                Role.MEMBER,
                                Provider.GOOGLE
                        )
                ));

        String role = member.getRole().name();

        String accessToken = jwtTokenProvider.createAccessToken(email, role);
        String refreshToken = jwtTokenProvider.createRefreshToken(email, role);

        String targetUrl = UriComponentsBuilder
                .fromUriString("http://localhost:5173/AIBE4_FinalProject_Team2_FE/oauth/callback")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}