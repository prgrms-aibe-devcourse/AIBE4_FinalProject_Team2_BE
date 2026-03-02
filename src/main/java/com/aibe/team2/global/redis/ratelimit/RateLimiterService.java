package com.aibe.team2.global.redis.ratelimit;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimiterService {
    private final StringRedisTemplate stringRedisTemplate;

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

        // 원자적 증가 연산
        Long currentCount = stringRedisTemplate.opsForValue().increment(rateLimiterKey);

        // 키가 최초 생성된 경우(값이 1인 경우)에만 만료 시간 할당
        if(currentCount != null && currentCount == 1L) {
            stringRedisTemplate.expire(rateLimiterKey, duration);
        }

        // 임계치 검증 후 반환
        return currentCount != null && currentCount <= maxRequests;
    }
}
