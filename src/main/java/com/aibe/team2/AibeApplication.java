package com.aibe.team2;

import com.aibe.team2.domain.error.config.ErrorAlertProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
@EnableScheduling
@EnableAsync
@EnableJpaAuditing
@EnableConfigurationProperties(ErrorAlertProperties.class)
@SpringBootApplication(exclude = {
        SecurityAutoConfiguration.class
})
public class AibeApplication {
    public static void main(String[] args) {
        SpringApplication.run(AibeApplication.class, args);
    }
}