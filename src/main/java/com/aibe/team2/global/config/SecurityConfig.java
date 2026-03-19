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
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/api/interviews/**",
                                "/actuator/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/h2-console/**",
                                "/swagger-ui.html",
                                "/api-docs/**",
                                "/api-docs",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/api/v1/mypage/**",
                                "/api/v1/notifications/**",
                                "/api/v1/resumes/**",
                                "/api/v1/job-postings/**",
                                "/resume.html",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()
                        .requestMatchers("/api/files/**", "/api/v1/auth/**").permitAll()
                        // [FR-INT-04] 외부 Retell AI Webhook 수신을 위한 권한 예외 처리 추가
                        .requestMatchers("/api/webhooks/**").permitAll()
                        // [NFR-LOG-01] 구조화 로그 설정(테스트를 위해 permitAll()로 변경)
                        .requestMatchers("/api/v1/admin/logs/**").hasRole("ADMIN")
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
                                .userService(customOAuth2UserService) // 이 부분!
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
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .httpBasic(basic -> basic.disable())
                .formLogin(login -> login.disable());

        return http.build();
    }
}