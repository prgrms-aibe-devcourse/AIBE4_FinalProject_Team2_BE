package com.aibe.team2.global.redis.lock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    // 락의 식별자 접두사 (예: "resume-analysis")
    String key();

    // 락 획득 대기 시간 (기본 3초)
    long waitTime() default 3L;

    // 락 소유 시간 (기본 5초: 로직이 안 끝나도 5초 뒤 자동 해제하여 데드락 방지)
    long leaseTime() default 5L;
}