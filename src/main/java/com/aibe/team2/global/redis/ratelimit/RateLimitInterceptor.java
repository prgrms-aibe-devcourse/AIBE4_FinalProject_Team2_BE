package com.aibe.team2.global.redis.ratelimit;

import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
// 💡 핵심 해결: implements HandlerInterceptor 추가
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiterService rateLimiterService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if(!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // 주의: 이 부분에서 에러가 난다면 RateLimit.java(어노테이션) 파일이 생성되어 있는지 꼭 확인해 줘!
        RateLimit rateLimit = handlerMethod.getMethodAnnotation(RateLimit.class);
        if(rateLimit == null) {
            return true;
        }

        String clientIdentifier = getClientIdentifier(request);
        String rateLimitKey = clientIdentifier + ":" + request.getRequestURI();

        Duration duration = Duration.ofSeconds(rateLimit.durationSeconds());
        boolean isAllowed = rateLimiterService.isAllowed(rateLimitKey, rateLimit.maxRequests(), duration);

        if (!isAllowed) {
            log.warn("Rate limit exceeded for identifier: {}", clientIdentifier);
            throw new BusinessException(ErrorCode.COMMON_429);
        }

        return true;
    }

    private String getClientIdentifier(HttpServletRequest request) {
        return request.getRemoteAddr();
    }

    // TODO : Spring Security 구현 후 수정
    // private String getClientIdentifier(HttpServletRequest request) {
    //     // 1. SecurityContextHolder에서 현재 요청의 인증(Authentication) 정보를 가져옴
    //     Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    //
    //     // 2. 인증 객체가 존재하고, 사용자가 로그인한 상태(인증됨)인지 검증
    //     if (authentication != null && authentication.isAuthenticated()) {
    //
    //         // 3. 익명 사용자(비로그인 상태)가 아닌 경우에만 해당 사용자의 고유 ID를 반환
    //         // principal이 "anonymousUser"인 경우는 Spring Security의 기본 익명 사용자 설정임
    //         Object principal = authentication.getPrincipal();
    //         if (!principal.equals("anonymousUser")) {
    //             return authentication.getName(); // 보통 JWT의 subject(유저 식별자)가 반환됨
    //         }
    //     }
    //
    //     // 4. Fallback (대체 수단): 비로그인 사용자가 접근 가능한 API의 경우 기존처럼 IP 주소 반환
    //     return request.getRemoteAddr();
    // }
}