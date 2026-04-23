package com.aibe.team2.global.config;

import com.aibe.team2.domain.auth.filter.JwtAuthenticationFilter;
import com.aibe.team2.domain.auth.filter.OAuth2SuccessHandler;
import com.aibe.team2.domain.auth.service.CustomMemberDetailService;
import com.aibe.team2.domain.auth.service.CustomOAuth2UserService;
import com.aibe.team2.domain.auth.util.JwtTokenProvider;
import com.aibe.team2.domain.log.filter.LoggingFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomMemberDetailService customMemberDetailService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final CorsConfigurationSource corsConfigurationSource;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())

                // 보안 헤더 설정
                .headers(headers -> headers
                        // 클릭재킹 방지 (우리 사이트의 프레임 내에서만 렌더링 허용)
                        .frameOptions(frame -> frame.sameOrigin())
//                        // XSS 방어 및 스크립트 실행 제한 (CSP)
//                        .contentSecurityPolicy(csp -> csp
//                                .policyDirectives(
//                                        "default-src 'self'; " +                    // 기본적으로 자신의 도메인만 허용
//                                                "script-src 'self' https://trusted.com; " + // 스크립트는 본인과 신뢰된 도메인만
//                                                "style-src 'self' 'unsafe-inline'; " +      // 인라인 스타일은 필요한 경우만 허용
//                                                "img-src 'self' data: https:; " +           // 이미지 출처 제한
//                                                "connect-src 'self' ws://localhost:8080;"   // WebSocket 연결 허용 (중요!)
//                                )
//                        )
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/", "/index.html", "/favicon.ico", "/css/**", "/js/**", "/images/**", "/api/v1/auth/**",
                                "/api/webhooks/**" // [FR-INT-04] 외부 Retell AI Webhook 수신을 위한 권한 예외 처리 추가
                        ).permitAll()

                        // Swagger 등 개발 도구
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**").permitAll()
                        .anyRequest().authenticated()
                )
                // LoggingFilter를 JwtFilter보다 먼저 실행되도록 등록
                .addFilterBefore(new LoggingFilter(redisTemplate, objectMapper),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider, customMemberDetailService),
                        UsernamePasswordAuthenticationFilter.class
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)
                        )
                        .successHandler(oAuth2SuccessHandler)
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(401);
                            response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"인증이 필요하거나 유효하지 않은 요청입니다.\"}");
                        })
                )
                .httpBasic(basic -> basic.disable())
                .formLogin(login -> login.disable());

        return http.build();
    }
}