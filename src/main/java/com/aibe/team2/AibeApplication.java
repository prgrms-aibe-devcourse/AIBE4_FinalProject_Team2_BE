package com.aibe.team2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication(exclude = {
        SecurityAutoConfiguration.class
})
public class AibeApplication {
    public static void main(String[] args) {
        SpringApplication.run(AibeApplication.class, args);
    }
}