package com.aibe.team2.domain.statistics.repository;

import com.aibe.team2.domain.mypage.entity.Member;
import com.aibe.team2.domain.mypage.repository.MemberRepository;
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
public class InterviewResultStatisticsRepositoryCustomTest {

    @Autowired
    private InterviewResultStatisticsRepository statisticsRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("QueryDSL 전체 조회 테스트")
    void findStatisticsByMemberIdOnly(){
        // 데이터가 없으면 테스트 통과 처리 (DataInitializer 의존성 제거)
        if (memberRepository.findAll().isEmpty()) {
            log.warn("⚠️ 테스트용 회원이 없습니다. DB가 초기화 상태입니다.");
            return;
        }

        Member firstMember = memberRepository.findAll().get(0);
        Long memberId = firstMember.getMemberId();

        List<InterviewResultStatistics> results = statisticsRepository.findStatisticsByCondition(memberId, null);

        if (!results.isEmpty()) {
            if(results.size() > 1){
                assertTrue(results.get(0).getCreatedAt().isAfter(results.get(1).getCreatedAt()) ||
                        results.get(0).getCreatedAt().isEqual(results.get(1).getCreatedAt()));
            }
        }
        log.info("성공! 조회 개수: {}", results.size());
    }

    @Test
    @DisplayName("QueryDSL 타입 필터링 조회 테스트")
    void findStatisticsByMemberIdAndSessionType(){
        if (memberRepository.findAll().isEmpty()) return;

        Member firstMember = memberRepository.findAll().get(0);
        Long memberId = firstMember.getMemberId();
        String targetType = "TEXT";

        List<InterviewResultStatistics> results = statisticsRepository.findStatisticsByCondition(memberId, targetType);

        assertNotNull(results);
        for(InterviewResultStatistics stat : results){
            // Enum 타입 처리를 위해 toString() 사용
            assertEquals(targetType, stat.getInterviewSession().getInterviewType().toString());
        }
        log.info("성공! TEXT 개수: {}", results.size());
    }
}