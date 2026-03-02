package com.aibe.team2.global.redis.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// API Rate Limiting 적용을 위한 커스텀 어노테이션
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    // 허용되는 최대 요청 횟수(기본값 10회)
    int maxRequests() default 10;

    // 제한을 두는 기준 시간(기본값 10초)
    long durationSeconds() default 10;
}
