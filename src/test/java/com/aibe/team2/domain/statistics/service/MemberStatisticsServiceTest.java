package com.aibe.team2.domain.statistics.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.aibe.team2.domain.statistics.dto.usage.MonthlyUsageResponse;
import com.aibe.team2.domain.statistics.dto.usage.MonthlyUsageStatDto;
import com.aibe.team2.domain.statistics.repository.usage.UsageLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class) // [추가] Mockito 환경 설정
class MemberStatisticsServiceTest {

    @Mock
    private UsageLogRepository usageLogRepository; // [추가] 목 객체 선언

    @InjectMocks
    private MemberStatisticsService memberStatisticsService;

    @Test
    @DisplayName("통계 조회 성공: DB에서 집계된 DTO를 받아 합계를 정확히 계산해야 한다.")
    void getMonthlyAiUsage_Success() {
        // [Given]
        Long memberId = 1L;
        int year = 2026;

        // 레포지토리가 반환할 '집계된' 가짜 데이터(DTO)를 직접 생성합니다.
        MonthlyUsageStatDto febResume = new MonthlyUsageStatDto(2, "RESUME", 3L, 300L);
        MonthlyUsageStatDto marInterview = new MonthlyUsageStatDto(3, "INTERVIEW", 1L, 500L);

        List<MonthlyUsageStatDto> mockStats = List.of(febResume, marInterview);

        // Mocking: 이제 리포지토리는 DTO 리스트를 반환합니다.
        given(usageLogRepository.findMonthlyStats(memberId, year))
                .willReturn(mockStats);

        // [When]
        MonthlyUsageResponse response = memberStatisticsService.getMonthlyUsageStatistics(memberId, year);

        // [Then]
        // 1. 전체 합계 검증 (300 + 500 = 800)
        assertThat(response.getTotalAmount()).isEqualTo(800);
        assertThat(response.getMonthlyStats()).hasSize(2);

        // 2. 데이터 정합성 검증
        assertThat(response.getMonthlyStats().get(0).getServiceType()).isEqualTo("RESUME");
        assertThat(response.getMonthlyStats().get(0).getAmount()).isEqualTo(300);

        verify(usageLogRepository).findMonthlyStats(memberId, year);
    }
}