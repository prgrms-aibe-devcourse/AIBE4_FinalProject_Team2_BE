package com.aibe.team2.global.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestTraceFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestTraceFilter.class);

    private static final String TRACE_ID_KEY = "trace_id";
    private static final String TRACE_ID_HEADER = "X-Request-ID";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String traceId = Optional
                .ofNullable(request.getHeader(TRACE_ID_HEADER))
                .filter(id -> !id.isBlank())
                .orElse(UUID.randomUUID().toString());

        MDC.put(TRACE_ID_KEY, traceId);
        MDC.put("method", request.getMethod());
        MDC.put("uri", request.getRequestURI());

        response.setHeader(TRACE_ID_HEADER, traceId);

        long start = System.currentTimeMillis();

        try {
            log.info("HTTP 요청 시작");

            filterChain.doFilter(request, response);

            long duration = System.currentTimeMillis() - start;
            log.info("HTTP 요청 종료 status={} durationMs={}", response.getStatus(), duration);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - start;
            log.error("HTTP 요청 실패 status={} durationMs={}", response.getStatus(), duration, e);
            throw e;
        } finally {
            MDC.remove("trace_id");
            MDC.remove("method");
            MDC.remove("uri");
        }
    }
}