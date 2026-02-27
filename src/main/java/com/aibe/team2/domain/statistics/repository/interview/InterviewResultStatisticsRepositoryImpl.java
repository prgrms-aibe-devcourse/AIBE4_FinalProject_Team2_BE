package com.aibe.team2.domain.statistics.repository.interview;

import com.aibe.team2.domain.statistics.entity.InterviewResultStatistics;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
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

    @Override
    public Tuple findAverageMetricsTupleByMemberIdAndCreatedAtBetween(Long memberId, LocalDateTime start, LocalDateTime end) {
        return queryFactory
                .select(
                        interviewResultStatistics.avgClarity.avg(),
                        interviewResultStatistics.avgPersuasiveness.avg(),
                        interviewResultStatistics.avgConsistency.avg(),
                        interviewResultStatistics.jobRelevanceScore.avg(),
                        interviewResultStatistics.logicalStructureScore.avg(),
                        interviewResultStatistics.attitudeConfidenceScore.avg()
                )
                .from(interviewResultStatistics)
                .join(interviewResultStatistics.interviewSession, interviewSession)
                .where(
                        memberIdEq(memberId), // 기존에 만들어둔 재사용 가능한 조건 메서드 활용
                        createdAtBetween(start, end) // 기간 필터링 조건 메서드 호출
                )
                .fetchOne();
    }

    // 동적 쿼리를 위한 BooleanExpression 메서드
    // 1. 회원 ID 일치 여부(필수 조건)
    private BooleanExpression memberIdEq(Long memberId) {
        if(memberId == null){
            throw new IllegalArgumentException("회원 ID는 필수입니다.");
        }
        return interviewSession.memberId.eq(memberId);
    }

    // 2. 면접 타입 일치 여부(동적 조건)
    private BooleanExpression sessionTypeEq(String sessionType) {

        return StringUtils.hasText(sessionType) ? interviewSession.interviewType.eq(sessionType) : null;
    }

    // 3. 생성일자 기간 필터링 조건
    private BooleanExpression createdAtBetween(LocalDateTime start, LocalDateTime end) {
        if(start == null || end == null) {
            return null;
        }
        return interviewResultStatistics.createdAt.between(start, end);
    }
}
