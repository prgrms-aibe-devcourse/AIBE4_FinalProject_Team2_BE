package com.aibe.team2;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest // 스프링 부트 환경을 띄워서 테스트 진행
@ActiveProfiles("test")
class RedisConnectionTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void redisConnectionAndDataTransferTest() {
        // given (준비)
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        String testKey = "test_token";
        String testValue = "eyJh... (임시 토큰 값)";

        // when (실행)
        valueOperations.set(testKey, testValue); // Redis에 저장
        String savedValue = (String) valueOperations.get(testKey); // Redis에서 조회

        // then (검증)
        System.out.println("Redis에서 꺼낸 값: " + savedValue);
        assertThat(savedValue).isEqualTo(testValue); // 저장한 값과 꺼낸 값이 같은지 확인
    }
}