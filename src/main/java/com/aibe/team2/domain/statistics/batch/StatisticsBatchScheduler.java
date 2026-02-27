package com.aibe.team2.domain.statistics.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatisticsBatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job dailyStatisticsJob;

    // 매일 00시 30분에 실행(Cron 표현식) - 초 분 시 일 월 요일
    @Scheduled(cron = "0 30 0 * * *")
    public void runDailyStatisticsJob() {
        // 배치 실행의 고유성을 위한 파라미터(어제 날짜 기준)
        String targetDate = LocalDate.now().minusDays(1).toString();
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("targetDate", targetDate)
                .addLong("time", System.currentTimeMillis())
                .toJobParameters();

        try{
            log.info("일별 통계 집계 배치 실행 시작 - 대상 일자: {}", targetDate);
            jobLauncher.run(dailyStatisticsJob, jobParameters);
            log.info("일별 통계 집계 배치 실행 완료");
        } catch (Exception e) {
            log.error("일별 통계 집계 배치 실행 중 오류 발생", e);
        }
    }
}
