package com.aibe.team2.domain.statistics.service;

import com.aibe.team2.domain.interview.repository.InterviewSessionRepository;
import com.aibe.team2.domain.resume.repository.ResumeAnalysisRepository;
import com.aibe.team2.domain.statistics.dto.DailyStatisticsResponse;
import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyStatisticsService {

    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final InterviewSessionRepository interviewSessionRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    // 일 단위 통계 조회
    public DailyStatisticsResponse getDailyStatistics(Long memberId, String targetDateStr) {

        LocalDate targetDate;

        // 1. 날짜 파싱 및 예외 전환
        try{
            targetDate = (targetDateStr == null)
                    ? LocalDate.now()
                    : LocalDate.parse(targetDateStr, DateTimeFormatter.ISO_DATE);
        } catch (DateTimeParseException e) {
            log.warn("잘못된 날짜 형식 요청: {}", targetDateStr);
            throw new BusinessException(ErrorCode.COMMON_408);
        }

        // 2. Redis Key 생성
        String redisKey = "status:user:" + memberId + ":" + targetDate.toString();
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();

        // 3. Cache Hit : Redis에 데이터가 있는지 확인
        DailyStatisticsResponse cachedResponse = (DailyStatisticsResponse) operations.get(redisKey);
        if(cachedResponse != null){
            return cachedResponse;
        }

        // 4. Cache Miss : DB에서 데이터 집계
        log.info("Cache Miss 발생! DB 집계 시작! 키: {}", redisKey);
        DailyStatisticsResponse statsDto = aggregateFromDb(memberId, targetDate);

        // 5. Redis에 저장(TTL 설정 : 자정까지)
        operations.set(redisKey, statsDto, Duration.ofDays(1));

        return statsDto;
    }

    // DB 집계 로직 분리
    private DailyStatisticsResponse aggregateFromDb(Long memberId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        // 첨삭 건수
        int resumeCount = (int) resumeAnalysisRepository.countByMemberIdAndCreatedAtBetween(memberId, startOfDay, endOfDay);

        // 면접 건수
        int interviewCount = (int) interviewSessionRepository.countByMemberIdAndCreatedAtBetween(memberId, startOfDay, endOfDay);

        // DTO 생성 및 반환
        return DailyStatisticsResponse.of(date, resumeCount, interviewCount);
    }
}
