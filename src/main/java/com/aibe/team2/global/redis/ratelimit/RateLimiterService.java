package com.aibe.team2.global.redis.ratelimit;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class RateLimiterService {
    private final StringRedisTemplate stringRedisTemplate;

    // 1. Lua 스크립트 상수로 정의
    private static final String RATE_LIMIT_SCRIPT =
            "local current = redis.call('INCR', KEYS[1]) " +
                    "if current == 1 then " +
                    "    redis.call('EXPIRE', KEYS[1], ARGV[1]) " +
                    "end " +
                    "return current";

    // 2. 실행할 스크립트 객체 미리 생성
    private final RedisScript<Long> redisScript = new DefaultRedisScript<>(RATE_LIMIT_SCRIPT, Long.class);

    /*
     * API 호출 처리율 제한(Rate Limiting) 검증 로직
     *
     * @param key 식별 키 (Client IP 또는 User ID)
     * @param maxRequests 허용되는 최대 요청 횟수 (Maximum Requests)
     * @param duration 제한 기준 시간 (Time Window)
     * @return 허용 여부 (true: 통과, false: 차단)
     */

    public boolean isAllowed(String key, int maxRequests, Duration duration){
        String rateLimiterKey = "rate_limiter:" + key;

        // 3. 스크립트 실행(원자적 연산(증가))
        Long currentCount = stringRedisTemplate.execute(
                redisScript,
                Collections.singletonList(rateLimiterKey),      // KEYS[1]에 매핑될 데이터
                String.valueOf(duration.getSeconds())           // ARGV[1]에 매핑될 데이터 (초 단위 변환)
        );

        // 4. 임계치 검증 후 반환
        return currentCount != null && currentCount <= maxRequests;
    }
}
