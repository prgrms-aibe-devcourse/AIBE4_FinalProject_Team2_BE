package com.aibe.team2.domain.statistics.batch;

import com.aibe.team2.domain.interview.repository.InterviewSessionRepository;
import com.aibe.team2.domain.mypage.entity.Member;
import com.aibe.team2.domain.mypage.repository.member.MemberRepository;
import com.aibe.team2.domain.resume.repository.ResumeAnalysisRepository;
import com.aibe.team2.domain.statistics.entity.DailyStatistics;
import com.aibe.team2.domain.statistics.repository.DailyStatisticsRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;

import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class StatisticsBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final MemberRepository memberRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final InterviewSessionRepository interviewSessionRepository;
    private final DailyStatisticsRepository dailyStatisticsRepository;

    private static final int CHUNK_SIZE = 100;

    // Job 설정(전체 배치 작업)
    @Bean
    public Job statisticsBatchJob() {
        return new JobBuilder("statisticsBatchJob", jobRepository)
                .start(dailyStatisticsStep())
                .build();
    }

    // Step 설정(데이터 읽기 -> 가공 -> 쓰기 과정 정의)
    @Bean
    public Step dailyStatisticsStep() {
        return new StepBuilder("dailyStatisticsStep", jobRepository)
                .<Member, DailyStatistics> chunk(CHUNK_SIZE, transactionManager)
                .reader(memberReader())
                .processor(statisticsProcessor(null)) // null은 런타임에 JobParameter로 주입됨
                .writer(statisticsWriter())
                .build();
    }

    // ItemReader : Member 엔티티를 페이징 단위(Chunk)로 읽어옴
    @Bean
    @StepScope
    public RepositoryItemReader<Member> memberReader() {
        return new RepositoryItemReaderBuilder<Member>()
                .name("memberReader")
                .repository(memberRepository)
                .methodName("findAll")
                .pageSize(CHUNK_SIZE)
                .sorts(Collections.singletonMap("memberId", Sort.Direction.ASC))
                .build();
    }

    // ItemProcessor : Member 데이터를 받아 DailyStatistics로 변환
    @Bean
    @StepScope
    public ItemProcessor<Member, DailyStatistics> statisticsProcessor(
            @Value("#{jobParameters['targetDate']}") String targetDateStr
    ) {
        return member -> {
            // 배치 실행 시 파라미터가 없으면 전날을 기준으로 삼음
            LocalDate targetDate = (targetDateStr != null)
                    ? LocalDate.parse(targetDateStr, DateTimeFormatter.ISO_DATE)
                    : LocalDate.now().minusDays(1);

            LocalDateTime startOfDay = targetDate.atStartOfDay();
            LocalDateTime endOfDay = targetDate.atTime(LocalTime.MAX);

            Long currentMemberId = member.getMemberId();

            // 회원별 통계 조회 (00:00:00 ~ 23:59:59)
            int resumeCount = (int) resumeAnalysisRepository.countByMemberIdAndCreatedAtBetween(currentMemberId, startOfDay, endOfDay);
            int interviewCount = (int) interviewSessionRepository.countByMemberIdAndCreatedAtBetween(currentMemberId, startOfDay, endOfDay);

            // 활동이 아예 없는 회원은 통계 테이블에 넣지 않음
            if(resumeCount == 0 && interviewCount == 0){
                return null;
            }

            // 멱등성 보장
            DailyStatistics statistics = dailyStatisticsRepository.findByMemberIdAndStatsDate(currentMemberId, targetDate)
                    .orElseGet(() -> DailyStatistics.builder()
                            .member(member)
                            .statsDate(targetDate)
                            .totalResumeCount(0) // 초기값 0
                            .totalInterviewCount(0) // 초기값 0
                            .build());

            // 조회된 건수를 객체에 업데이트
            statistics.updateCounts(resumeCount, interviewCount);

            return statistics;
        };
    }

    // ItemWriter : 완성된 통계 데이터를 DB에 저장
    @Bean
    @StepScope
    public RepositoryItemWriter<DailyStatistics> statisticsWriter() {
        return new RepositoryItemWriterBuilder<DailyStatistics>()
                .repository(dailyStatisticsRepository)
                .methodName("save")
                .build();
    }
}
