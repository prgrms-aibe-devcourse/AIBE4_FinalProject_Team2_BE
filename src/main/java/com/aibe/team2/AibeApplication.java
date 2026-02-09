package com.aibe.team2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 아래의 코드는 전부 예시입니다. 추후 기능이나 필요에 맞게 자유롭게 수정하면 됩니다.
 * @SpringBootApplication: 스프링 부트의 자동 설정, 빈 스캐닝 등을 활성화합니다.
 * @EnableJpaAuditing: 엔티티의 생성일/수정일을 자동으로 관리하기 위해 필요합니다.
 */
@EnableJpaAuditing
@SpringBootApplication
public class AibeApplication {

    public static void main(String[] args) {
        // 앱 실행 시 가장 먼저 호출되는 메인 메서드입니다.
        SpringApplication.run(AibeApplication.class, args);
    }

}