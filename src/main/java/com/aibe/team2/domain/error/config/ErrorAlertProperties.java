package com.aibe.team2.domain.error.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.error.alert")
public class ErrorAlertProperties {

    /**
     * 동일 이슈 발생 횟수 임계치
     */
    private long occurrenceThreshold = 10;
}