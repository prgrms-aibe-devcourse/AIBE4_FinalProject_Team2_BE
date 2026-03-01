package com.aibe.team2.domain.statistics.repository.usage;

import com.aibe.team2.domain.statistics.dto.usage.MonthlyUsageStatResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.aibe.team2.domain.statistics.entity.QUsageLog.usageLog;

@RequiredArgsConstructor
public class UsageLogRepositoryImpl implements UsageLogRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<MonthlyUsageStatResponse> findMonthlyStats(Long memberId, int year) {

        // [1] 날짜에서 '월' 추출
        NumberTemplate<Integer> monthExpression = Expressions.numberTemplate(
                Integer.class, "function('month', {0})", usageLog.createdAt
        );

        return queryFactory
                .select(Projections.constructor(
                        MonthlyUsageStatResponse.class,
                        monthExpression,
                        usageLog.serviceType.stringValue(),
                        usageLog.count(),
                        usageLog.amount.sum().longValue() // Integer sum을 Long으로 변환
                ))
                .from(usageLog)
                .where(
                        // [주의] memberId가 아니라 member.memberId로 접근해야 합니다.
                        usageLog.member.memberId.eq(memberId),
                        usageLog.createdAt.year().eq(year)
                )
                .groupBy(monthExpression, usageLog.serviceType)
                .orderBy(monthExpression.asc())
                .fetch();
    }
}