package com.aibe.team2.domain.statistics.service;

import com.aibe.team2.domain.statistics.dto.usage.MonthlyUsageResponse;
import com.aibe.team2.domain.statistics.dto.usage.MonthlyUsageStatDto;
import com.aibe.team2.domain.statistics.repository.usage.UsageLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberStatisticsService {

    private final UsageLogRepository usageLogRepository;

    /*
     * 월별 사용량 통계 조회
     * @param memberId 사용자 ID
     * @param year 조회할 연도
     * @return 월별 통계 리스트와 총합이 담긴 응답 객체
     */
    public MonthlyUsageResponse getMonthlyUsageStatistics(Long memberId, int year){

        // 1. Repository를 통해 DB에서 월별/서비스별 통계 데이터를 가져옴
        List<MonthlyUsageStatDto> stats = usageLogRepository.findMonthlyStats(memberId, year);

        // 2. 미리 만들어둔 ResponseDTO의 static 메서드를 사용해 결과를 포장
        return MonthlyUsageResponse.of(memberId, year, stats);
    }
}
