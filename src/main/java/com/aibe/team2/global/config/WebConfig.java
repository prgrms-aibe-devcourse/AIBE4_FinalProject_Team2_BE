package com.aibe.team2.global.config; // 패키지명은 프로젝트 구조에 맞게 조정

import com.aibe.team2.global.common.resolver.LoginMemberIdResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final LoginMemberIdResolver loginMemberIdResolver;
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 주소 허용
                .allowedOrigins("*") // 모든 출처 허용 (개발용)
                .allowedMethods("GET", "POST", "PUT", "DELETE");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginMemberIdResolver);
    }
}