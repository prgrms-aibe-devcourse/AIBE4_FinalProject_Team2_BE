package com.aibe.team2.domain.auth.service;

import com.aibe.team2.domain.auth.entity.SocialAuth;
import com.aibe.team2.domain.auth.repository.SocialAuthRepository;
import com.aibe.team2.domain.auth.util.CustomUserDetails;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final SocialAuthRepository socialAuthRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 1. 소셜 제공자 정보 추출 (google, github 등)
        String providerType = userRequest.getClientRegistration().getRegistrationId().toUpperCase();
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 2. 소셜 고유 식별자 및 이메일 추출
        String providerMemberId = String.valueOf(attributes.get(userNameAttributeName));
        String email = null;
        String name = null;

        // --- 각 제공자별 맞춤 추출 로직 시작 ---
        if ("GOOGLE".equals(providerType)) {
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
        }
        else if ("GITHUB".equals(providerType)) {
            email = (String) attributes.get("email");
            // 깃허브는 name이 null일 수 있으므로 login(ID)을 대안으로 사용
            name = (attributes.get("name") != null) ? (String) attributes.get("name") : (String) attributes.get("login");
        }
        else if ("KAKAO".equals(providerType)) {
            // 카카오는 kakao_account -> profile -> nickname 구조입니다.
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            Map<String, Object> kakaoProfile = (Map<String, Object>) kakaoAccount.get("profile");

            email = (String) kakaoAccount.get("email");
            name = (String) kakaoProfile.get("nickname"); // 카카오는 보통 nickname을 사용
        }
        // --- 추출 로직 끝 ---

        // 방어 코드
        if (name == null) {
            name = (email != null) ? email.split("@")[0] : "SocialUser";
        }
        if (email == null) {
            email = providerType.toLowerCase() + "_" + providerMemberId + "@social.com";
        }

        // 3. 소셜 연동 정보 확인 및 회원 처리
        Member member = processSocialLogin(email, name, providerType, providerMemberId);

        return new CustomUserDetails(member, attributes);
    }

    private Member processSocialLogin(String email, String name, String providerType, String providerMemberId) {
        // 1. 먼저 social_auth 테이블에서 해당 소셜 계정이 연동되어 있는지 확인
        return socialAuthRepository.findByProviderMemberIdAndProviderType(providerMemberId, providerType)
                .map(SocialAuth::getMember) // 이미 연동된 계정이면 연결된 Member 반환
                .orElseGet(() -> {
                    // 2. 연동 정보가 없다면, 이메일로 기존 일반 회원이 있는지 확인
                    Member member = memberRepository.findByEmail(email)
                            .orElseGet(() -> registerNewMember(email, name, providerType)); // 없으면 신규 가입

                    // 3. 신규 소셜 연동 정보(SocialAuth) 생성 및 저장
                    saveSocialAuth(member, providerType, providerMemberId);
                    return member;
                });
    }

    private Member registerNewMember(String email, String name, String provider) {
        Member newMember = new Member(email, "SOCIAL_AUTH_USER", name, Role.MEMBER, Provider.valueOf(provider));
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

    private Member saveOrUpdate(String email, String name, Provider provider) {
        Member member = memberRepository.findByEmail(email)
                .orElse(new Member(email, "SOCIAL_LOGIN", name, Role.MEMBER, provider));

        return memberRepository.save(member);
    }
}