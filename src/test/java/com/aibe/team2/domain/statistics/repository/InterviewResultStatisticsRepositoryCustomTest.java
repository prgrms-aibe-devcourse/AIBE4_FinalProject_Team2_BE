package com.aibe.team2.domain.statistics.repository;

import com.aibe.team2.domain.mypage.entity.Member;
import com.aibe.team2.domain.mypage.repository.MemberRepository;
import com.aibe.team2.domain.statistics.entity.InterviewResultStatistics;
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
@SpringBootTest(properties = {
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379",
        "GEMINI_API_KEY=dummy-key-for-test",
        "GEMINI_API_URL=dummy-url"
})
@ActiveProfiles("local") // DataInitializer를 작동시켜 더미 데이터 생성을 위함
@Transactional
public class InterviewResultStatisticsRepositoryCustomTest {

    @Autowired
    private InterviewResultStatisticsRepository statisticsRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("QueryDSL : 회원 ID만 주어졌을 때(sessionType =null), 해당 해원의 모든 통계 데이터를 내림차순으로 조회한다.")
    void findStatisticsByMemberIdOnly(){
        // [Given] 더미 데이터 중 첫 번째 회원의 ID를 가져옴
        Member firstMember = memberRepository.findAll().get(0);
        Long memberId = firstMember.getId();

        // [When] 세션 타입을 null로 넘겨서 동적 쿼리가 무시되는지 확인
        List<InterviewResultStatistics> results = statisticsRepository.findStatisticsByCondition(memberId, null);

        // [Then] 검증
        assertFalse(results.isEmpty(), "더미 데이터가 조회되어야 합니다.");

        // 정렬 검증(내림차순, 최신 데이터가 먼저 오는지)
        if(results.size() > 1){
            assertTrue(results.get(0).getCreatedAt().isAfter(results.get(1).getCreatedAt()) ||
                    results.get(0).getCreatedAt().isEqual(results.get(1).getCreatedAt()));
        }

        log.info("전체 조회 테스트 성공! 조회된 개수: {}", results.size());
    }

    @Test
    @DisplayName("QueryDSL: 회원 ID와 면접 타입(TEXT)가 주어졌을 때, 해당 조건에 맞는 데이터만 필터링하여 조회한다.")
    void findStatisticsByMemberIdAndSessionType(){
        // [Given] 더미 데이터 중 첫 번째 회원의 ID와 "TEXT" 타입 설정
        Member firstMember = memberRepository.findAll().get(0);
        Long memberId = firstMember.getId();
        String targetType = "TEXT";

        // [When] TEXT 타입 조건으로 조회
        List<InterviewResultStatistics> results = statisticsRepository.findStatisticsByCondition(memberId, targetType);

        // [Then] 검증
        assertNotNull(results);
        for(InterviewResultStatistics stat : results){
            // 조인된 세션 테이블의 타입이 "TEXT"가 맞는지 100% 확신 검증
            assertEquals(targetType, stat.getInterviewSession().getType(), "조회된 데이터 중 TEXT 타입이 아닌 것이 포함되어 있습니다.");
        }

        log.info("타입 필터링 조회 테스트 성공! 조회된 TEXT 면접 개수: {}", results.size());
    }
}
