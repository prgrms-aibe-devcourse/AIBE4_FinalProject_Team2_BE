package com.aibe.team2.domain.log.filter;

import com.aibe.team2.domain.log.dto.LogDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

//@Component
@RequiredArgsConstructor
@Slf4j
public class LoggingFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 로그인/로그아웃 등 특정 경로는 제외하고 싶다면 shouldNotFilter 활용
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        long start = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put("request_id", requestId);

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long end = System.currentTimeMillis();
            saveLogToRedis(requestWrapper, responseWrapper, requestId, (end - start));
            responseWrapper.copyBodyToResponse();
            MDC.clear();
        }
    }

    @Async
    protected void saveLogToRedis(ContentCachingRequestWrapper req, ContentCachingResponseWrapper res, String id, long time) {
        try {
            String today = LocalDate.now().toString(); // yyyy-MM-dd
            String redisKey = "api:logs:" + today;

            // 로그 객체 생성
            LogDTO dto = LogDTO.builder()
                    .timestamp(LocalDateTime.now().toString())
                    .requestId(id)
                    .method(req.getMethod())
                    .uri(req.getRequestURI())
                    .status(res.getStatus())
                    .clientIp(req.getRemoteAddr())
                    .userEmail(SecurityContextHolder.getContext().getAuthentication() != null ?
                            SecurityContextHolder.getContext().getAuthentication().getName() : "guest")
                    .elapsedTime(time)
                    .requestBody(new String(req.getContentAsByteArray()))
                    .responseBody(new String(res.getContentAsByteArray()))
                    .build();

            String logJson = dto.toJson(objectMapper);

            // Redis 저장 (최신 로그가 위로 오게 LPUSH)
            Long listSize = redisTemplate.opsForList().leftPush(redisKey, logJson);

            // 리스트가 새로 생성되었을 때(첫 아이템 추가 시)만 만료 시간 설정
            if (listSize != null && listSize == 1) {
                redisTemplate.expire(redisKey, 30, TimeUnit.DAYS);
            }

            log.info("Redis Log Saved: {}", id);

        } catch (Exception e) {
            log.error("Failed to save log to Redis", e);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // /error 경로나 정적 리소스는 로그 대상에서 제외하여 무한 루프 방지
        // SSE 스트리밍 요청("/stream")은 ContentCaching 필터를 타지 않도록 예외 처리
        // 작성자 최원준 / 면접관의 답변이 프론트에만 보이지 않는 현상 수정
        return path.startsWith("/error") ||
                path.startsWith("/favicon.ico") ||
                path.startsWith("/api/v1/admin/logs") ||
                path.contains("/stream"); // 작성자 최원준
    }
}