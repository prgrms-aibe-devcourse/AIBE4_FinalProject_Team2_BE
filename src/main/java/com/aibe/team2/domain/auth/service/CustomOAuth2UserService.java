package com.aibe.team2.domain.auth.service;

import com.aibe.team2.domain.auth.entity.SocialAuth;
import com.aibe.team2.domain.auth.repository.SocialAuthRepository;
import com.aibe.team2.domain.auth.dto.CustomUserDetails;
import com.aibe.team2.domain.mypage.entity.Member;
import com.aibe.team2.domain.mypage.entity.enums.Provider;
import com.aibe.team2.domain.mypage.entity.enums.Role;
import com.aibe.team2.domain.mypage.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final SocialAuthRepository socialAuthRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String providerType = userRequest.getClientRegistration().getRegistrationId().toUpperCase(); // GOOGLE, GITHUB, KAKAO
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        Map<String, Object> attributes = oAuth2User.getAttributes();

        String providerMemberId = String.valueOf(attributes.get(userNameAttributeName));
        String email = null;
        String name = null;

        // --- 각 제공자별 맞춤 추출 로직 시작 ---
        if ("GOOGLE".equals(providerType)) {
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
        } else if ("GITHUB".equals(providerType)) {
            // 깃허브는 public 이메일이 설정 안 된 경우가 많음
            Object emailObj = attributes.get("email");
            email = (emailObj != null) ? String.valueOf(emailObj) : null;

            Object nameObj = attributes.get("name");
            name = (nameObj != null) ? String.valueOf(nameObj) : String.valueOf(attributes.get("login"));
        } else if ("KAKAO".equals(providerType)) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            if (kakaoAccount != null) {
                email = (String) kakaoAccount.get("email");
                Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                if (profile != null) {
                    name = (String) profile.get("nickname");
                }
            }
        }
        // --- 추출 로직 끝 ---

        // 방어 코드: 여전히 name이 null이라면 최소한 이메일 앞부분이라도 할당
        if (name == null || name.isBlank()) {
            name = (email != null && email.contains("@")) ? email.split("@")[0] : "User_" + providerMemberId;
        }
        if (email == null || email.isBlank()) {
            // 고유 ID를 포함하여 절대 중복되지 않는 가상 이메일 생성
            email = providerType.toLowerCase() + "_" + providerMemberId + "@synctalk.com";
        }

        // 3. 소셜 연동 정보 확인 및 회원 처리 (provider 인자는 필요에 따라 providerType으로 대체 가능)
        Member member = processSocialLogin(email, name, providerType, providerMemberId);

        return new CustomUserDetails(member, attributes);
    }

    private Member processSocialLogin(String email, String name, String providerType, String providerMemberId) {
        // 1. 먼저 social_auth 테이블에서 해당 소셜 계정이 연동되어 있는지 확인
        return socialAuthRepository.findByProviderMemberIdAndProviderTypeWithMember(providerMemberId, providerType)
                .map(SocialAuth::getMember)
                .orElseGet(() -> {
                    // 2. 연동 정보가 없다면, 이메일로 기존 일반 회원이 있는지 확인
                    Member member = memberRepository.findByEmail(email)
                            .orElseGet(() -> registerNewMember(email, name, providerType)); // 없으면 신규 가입

                    // 3. 신규 소셜 연동 정보(SocialAuth) 생성 및 저장
                    saveSocialAuth(member, providerType, providerMemberId);
                    return member;
                });
    }

    private Member registerNewMember(String email, String name, String providerType) {
        Member newMember = new Member(email, "SOCIAL_AUTH_USER", name, Role.MEMBER, Provider.valueOf(providerType));
        return memberRepository.save(newMember);
    }

    private void saveSocialAuth(Member member, String providerType, String providerMemberId) {
        SocialAuth socialAuth = SocialAuth.builder()
                .member(member)
                .providerType(providerType)
                .providerMemberId(providerMemberId)
                .build();
        socialAuthRepository.save(socialAuth);
    }
}