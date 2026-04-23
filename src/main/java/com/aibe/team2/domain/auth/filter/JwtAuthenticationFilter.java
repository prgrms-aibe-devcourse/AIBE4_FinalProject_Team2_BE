package com.aibe.team2.domain.auth.filter;

import com.aibe.team2.domain.auth.service.CustomMemberDetailService;
import com.aibe.team2.domain.auth.util.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Cookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomMemberDetailService customMemberDetailService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, CustomMemberDetailService customMemberDetailService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.customMemberDetailService = customMemberDetailService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            String email = jwtTokenProvider.getEmail(token);

            // 1. DB에서 사용자 정보를 로드 (권한 정보 포함)
            UserDetails userDetails = customMemberDetailService.loadUserByUsername(email);

            // 2. userDetails.getAuthorities()를 통해 실제 권한을 부여
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities() // 빈 리스트 대신 실제 권한 주입!
            );

            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        // 1. Authorization 헤더 확인
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // 2. 쿠키에서 accessToken 확인
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        // [추가] 3. 쿼리 파라미터에서 token 확인 (EventSource 등 헤더 조작이 불가능한 SSE 통신 우회용)
        String queryToken = request.getParameter("token");
        if (StringUtils.hasText(queryToken)) {
            return queryToken;
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        // /api/v1/auth/로 시작하는 모든 요청(로그인, 회원가입 등)은 이 필터의 검사를 생략합니다.
        return path.startsWith("/api/v1/auth/");
    }
}