package com.aibe.team2.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

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

        // 5. 거절 정책 (RejectedExecutionHandler) - 111번째 주문이 들어왔을 때의 행동 강령!
        // CallerRunsPolicy: "요리사(비동기 쓰레드)가 다 바쁘면, 주문을 받은 카운터 직원(메인 쓰레드)이 직접 요리해라!"
        // 에러를 뱉어버리지 않고, 어떻게든 처리는 하도록 보장하는 아주 안전한 정책입니다.
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();
        return executor;
    }
}