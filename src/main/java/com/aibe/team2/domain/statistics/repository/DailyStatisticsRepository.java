package com.aibe.team2.domain.statistics.repository;

import com.aibe.team2.domain.statistics.entity.DailyStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyStatisticsRepository extends JpaRepository<DailyStatistics, Long> {

    // 특정 회원의 특정 기간 통계 조회(대시보드 그래프용)
    @Query("""
          select ds
          from DailyStatistics ds
          where ds.member.memberId = :memberId
            and ds.statsDate between :start and :end
    """)
    List<DailyStatistics> findAllByMemberAndStatsDateBetween(
            @Param("memberId") Long memberId,
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    // 특정 회원의 특정 날짜 통계 조회(중복 확인용 or 단건 조회용)
    @Query("""
      select ds
      from DailyStatistics ds
      where ds.member.memberId = :memberId
        and ds.statsDate = :statsDate
""")
    Optional<DailyStatistics> findByMemberIdAndStatsDate(
            @Param("memberId") Long memberId,
            @Param("statsDate") LocalDate statsDate
    );
}
