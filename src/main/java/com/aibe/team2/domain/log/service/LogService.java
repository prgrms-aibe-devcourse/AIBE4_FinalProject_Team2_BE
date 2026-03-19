package com.aibe.team2.domain.log.service;

import com.aibe.team2.domain.log.dto.LogDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LogService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 특정 날짜의 모든 로그 조회
     * @param date yyyy-MM-dd 형식
     */
    public List<LogDTO> getLogsByDate(String date) {
        String redisKey = "api:logs:" + date;

        // Redis List의 모든 데이터 가져오기 (0부터 -1까지)
        List<String> rawLogs = redisTemplate.opsForList().range(redisKey, 0, -1);

        if (rawLogs == null) return Collections.emptyList();

        // JSON 문자열을 다시 DTO 객체로 역직렬화
        return rawLogs.stream()
                .map(json -> {
                    try {
                        return objectMapper.readValue(json, LogDTO.class);
                    } catch (JsonProcessingException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}