package com.aibe.team2.global.common.resolver;

import com.aibe.team2.domain.auth.dto.CustomUserDetails;
import com.aibe.team2.global.common.annotation.LoginMemberId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoginMemberIdResolver implements HandlerMethodArgumentResolver {

    private final Environment env; // 실행 환경(dev, prod) 구분을 위한 주입

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginMemberId.class) &&
                parameter.getParameterType().equals(Long.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 1. 정상적인 인증 토큰이 있는 경우 (가장 이상적)
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            if (userDetails.getMember() != null) {
                return userDetails.getMember().getMemberId();
            }
        }

        // 2. 토큰이 없는 경우: 현재 환경이 'dev' 또는 'local'인지 확인 (스마트한 Fallback)
        boolean isDevEnvironment = Arrays.asList(env.getActiveProfiles()).contains("dev") ||
                Arrays.asList(env.getActiveProfiles()).contains("local");

        // TODO : 프론트엔드 API 연동 테스트 완료 후 실제 운영 환경 배포 전 하드코딩 삭제!
        // 개발 환경이라면 1L을 주고 테스트 통과
        if (isDevEnvironment) {
            log.warn("🚨 [DEV 환경] 인증 토큰이 없어 임시 테스트 ID(1L)를 반환합니다.");
            return 1L;
        }

        // 3. 운영 환경이거나 그 외 상황에서는 가차없이 예외 발생
        throw new AuthenticationCredentialsNotFoundException("인증된 사용자 정보가 없습니다.");
    }
}