package com.aibe.team2.domain.statistics.service;

import com.aibe.team2.domain.interview.repository.InterviewRepository;
import com.aibe.team2.domain.statistics.repository.UsageLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberStatisticsService {

    private final UsageLogRepository usageLogRepository;
    private final InterviewRepository interviewRepository;

    // 1. [FR-STA-01] 이번 달 AI 서비스 사용량 조회

}
