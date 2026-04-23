package com.aibe.team2.global.config;

import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "aiAnalysisTaskExecutor")
    public Executor aiAnalysisTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 1. 기본 출근 요리사 수
        executor.setCorePoolSize(5);

        // 2. 최대 고용 요리사 수
        executor.setMaxPoolSize(10);

        // 3. 대기열 크기
        executor.setQueueCapacity(100);

        // 4. 쓰레드 이름 접두사 (로그에서 쉽게 알아보기 위함)
        executor.setThreadNamePrefix("AI-Worker-");

        // 5. 거절 정책 (RejectedExecutionHandler)
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();
        return executor;
    }

    @Bean(name = "errorLogExecutor")
    public Executor errorLogExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("error-log-");

        // MDC(requestId)를 비동기 스레드에도 전파
        executor.setTaskDecorator(runnable -> {
            Map<String, String> mdcContext = MDC.getCopyOfContextMap();
            return () -> {
                if (mdcContext != null) MDC.setContextMap(mdcContext);
                try {
                    runnable.run();
                } finally {
                    MDC.clear();
                }
            };
        });

        executor.initialize();
        return executor;
    }
}