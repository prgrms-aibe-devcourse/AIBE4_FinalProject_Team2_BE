package com.aibe.team2.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 아래의 코드는 전부 예시입니다. 프로젝트 환경에 맞게 수정하세요.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AI Resume & Interview Coach API")
                        .description("자기소개서 코칭 및 면접 도우미 서비스 API 명세서")
                        .version("v1.0.0"));
    }
}