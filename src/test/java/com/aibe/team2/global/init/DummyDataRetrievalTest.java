package com.aibe.team2.global.init;

import com.aibe.team2.domain.statistics.dto.common.RadarChartStatResponse;
import com.aibe.team2.domain.statistics.entity.InterviewResultStatistics;
import com.aibe.team2.domain.statistics.repository.interview.InterviewResultStatisticsRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class DummyDataRetrievalTest {

   @Autowired
   private InterviewResultStatisticsRepository statisticsRepository;

   @Test
    @DisplayName("DB에 적재된 통계 더미 데이터가 RadarChartStatResponse DTO로 정상 변환된다.")
    void testDummyDataRetrievalAndDtoMapping(){
       // [Given]  DataInitializer에 의해 생성된 더미 데이터를 DB에서 전체 조회
       List<InterviewResultStatistics> statsList = statisticsRepository.findAll();

       // DB에 데이터가 1건 이상 존재하는지 확인(더미 데이터 생성 로직 검증)
       assertFalse(statsList.isEmpty(), "데이터베이스에 더미 데이터가 존재하지 않습니다.");
       InterviewResultStatistics entity = statsList.get(0); // 테스트를 위해 첫 번째 통계 데이터 추출

       // [When]  Entity 객체를 클라이언트 반환용 DTO 객체로 변환(Mapping)
       RadarChartStatResponse dto = RadarChartStatResponse.builder()
               .avgClarity(entity.getAvgClarity())
               .avgPersuasiveness(entity.getAvgPersuasiveness())
               .avgConsistency(entity.getAvgConsistency())
               .jobRelevanceScore(entity.getJobRelevanceScore())
               .logicalStructureScore(entity.getLogicalStructureScore())
               .attitudeConfidenceScore(entity.getAttitudeConfidenceScore())
               .build();

       // [Then] 변환된 DTO의 무결성 검증
       assertNotNull(dto, "DTO 반환 결과가 null입니다.");
       assertEquals(entity.getAvgClarity(), dto.getAvgClarity());
       assertEquals(entity.getJobRelevanceScore(), dto.getJobRelevanceScore());

       log.info("DTO 매핑 성공! 조회된 명확성 평균 점수 : {}", dto.getAvgClarity());
   }
}
