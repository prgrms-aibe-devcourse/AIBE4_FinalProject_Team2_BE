package com.aibe.team2.domain.statistics.repository;

import com.aibe.team2.domain.statistics.entity.InterviewResultStatistics;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.aibe.team2.domain.interview.entity.QInterviewSession.interviewSession;
import static com.aibe.team2.domain.statistics.entity.QInterviewResultStatistics.interviewResultStatistics;

@Repository
@RequiredArgsConstructor
public class InterviewResultStatisticsRepositoryImpl implements InterviewResultStatisticsRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<InterviewResultStatistics> findStatisticsByCondition(Long memberId, String sessionType) {
        return queryFactory
                .selectFrom(interviewResultStatistics)
                // 연관된 면접 세션 테이블과 조인(회원 ID 및 타입 검사를 위해)
                .join(interviewResultStatistics.interviewSession, interviewSession)
                .where(
                        memberIdEq(memberId), // 필수 조건
                        sessionTypeEq(sessionType) // 동적 조건(null이면 무시됨)
                )
                // 최신 면접 결과부터 나오도록 내림차순 정렬
                .orderBy(interviewResultStatistics.createdAt.desc())
                .fetch();
    }

    // 동적 쿼리를 위한 BooleanExpression 메서드
    // 1. 회원 ID 일치 여부(필수 조건)
    private BooleanExpression memberIdEq(Long memberId) {
        return memberId != null ? interviewSession.memberId.eq(memberId) : null;
    }

    // 2. 면접 타입 일치 여부(동적 조건)
    private BooleanExpression sessionTypeEq(String sessionType) {
        // StringUtils.hasText()를 통해 null 또는 빈 문자열을 한 번에 검증
        return StringUtils.hasText(sessionType) ? interviewSession.type.eq(sessionType) : null;
    }
}
