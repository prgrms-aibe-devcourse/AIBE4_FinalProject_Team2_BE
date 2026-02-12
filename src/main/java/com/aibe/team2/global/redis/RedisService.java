package com.aibe.team2.global.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    // 1. 데이터 저장(기본)
    public void setValues(String key, String value){
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        values.set(key, value);
    }

    // 2. 데이터 저장(만료시간 설정(ex. 5분 뒤 삭제))
    // 로그인 토큰 저장 시 필수
    public void setValues(String key, String value, Duration duration){
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        values.set(key, value, duration);
    }

    // 3. 데이터 조회
    public String getValues(String key){
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        if(values.get(key) == null){
            return "false";
        }
        return (String) values.get(key);
    }

    // 4. 데이터 삭제
    public void deleteValues(String key){
        redisTemplate.delete(key);
    }

    // 5, 키 존재 여부 확인
    public boolean hasKey(String key){
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
