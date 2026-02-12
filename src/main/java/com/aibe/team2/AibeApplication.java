package com.aibe.team2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


@EnableJpaAuditing
@SpringBootApplication
public class AibeApplication {
    public static void main(String[] args) {
        SpringApplication.run(AibeApplication.class, args);
    }

}