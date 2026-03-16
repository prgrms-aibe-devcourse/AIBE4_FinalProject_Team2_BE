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
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimiterService rateLimiterService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if(!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        // 이 부분에서 에러가 난다면 RateLimit.java(어노테이션) 파일이 생성되어 있는지 확인
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

    // 1. 클라이언트 식별자를 결정하는 메인 메서드
    private String getClientIdentifier(HttpServletRequest request) {

        // 1-1. SecurityContext에서 현재 인증된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 1-2. 인증 객체가 존재하고, 익명 사용자가 아닐 경우 (정상 로그인 상태)
        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            Object principal = authentication.getPrincipal();

            // 1-3. 우리가 만든 CustomUserDetails로 형변환(Casting)하여 memberId 추출
            if (principal instanceof CustomUserDetails userDetails) {
                return "MEMBER_" + userDetails.member().getMemberId();
            }
        }

        // 1-4. 비로그인 사용자이거나 인증 정보가 없을 경우, 실제 IP 주소 반환
        return "IP_" + getRealClientIp(request);
    }

    // 2. 프록시 환경을 고려하여 실제 클라이언트 IP를 추출하는 헬퍼 메서드
    private String getRealClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}