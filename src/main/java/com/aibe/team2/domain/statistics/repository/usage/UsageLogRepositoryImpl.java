package com.aibe.team2.domain.statistics.repository.usage;

import com.aibe.team2.domain.statistics.dto.admin.UsageLogAdminRow;
import com.aibe.team2.domain.statistics.dto.admin.UsageLogAdminSearchCond;
import com.aibe.team2.domain.statistics.dto.usage.MonthlyUsageStatResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberTemplate;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.time.LocalDateTime;

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

    @Override
    public Page<UsageLogAdminRow> searchAdminUsageLogs(UsageLogAdminSearchCond cond, Pageable pageable) {

        BooleanBuilder where = new BooleanBuilder();

        if (cond != null) {
            if (cond.getMemberId() != null) {
                where.and(usageLog.member.memberId.eq(cond.getMemberId()));
            }
            if (cond.getNickname() != null && !cond.getNickname().isBlank()) {
                where.and(usageLog.member.nickname.containsIgnoreCase(cond.getNickname()));
            }
            if (cond.getEmail() != null && !cond.getEmail().isBlank()) {
                where.and(usageLog.member.email.containsIgnoreCase(cond.getEmail()));
            }
            if (cond.getServiceType() != null) {
                where.and(usageLog.serviceType.eq(cond.getServiceType()));
            }
            if (cond.getTargetType() != null && !cond.getTargetType().isBlank()) {
                where.and(usageLog.targetType.eq(cond.getTargetType()));
            }
            if (cond.getFrom() != null) {
                LocalDateTime from = cond.getFrom().atStartOfDay();
                where.and(usageLog.createdAt.goe(from));
            }
            if (cond.getTo() != null) {
                LocalDateTime toExclusive = cond.getTo().plusDays(1).atStartOfDay();
                where.and(usageLog.createdAt.lt(toExclusive));
            }
        }

        List<UsageLogAdminRow> content = queryFactory
                .select(Projections.constructor(
                        UsageLogAdminRow.class,
                        usageLog.id,
                        usageLog.member.memberId,
                        usageLog.member.email,
                        usageLog.serviceType,
                        usageLog.amount,
                        usageLog.tokenUsage,
                        usageLog.balanceAfter,
                        usageLog.requestTraceId,
                        usageLog.targetType,
                        usageLog.targetId,
                        usageLog.description,
                        usageLog.createdAt
                ))
                .from(usageLog)
                .where(where)
                .orderBy(usageLog.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(usageLog.count())
                .from(usageLog)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    @Override
    public List<UsageLogAdminRow> findTop5AdminUsageLogs() {

        return queryFactory
                .select(Projections.constructor(
                        UsageLogAdminRow.class,
                        usageLog.id,
                        usageLog.member.memberId,
                        usageLog.member.email,
                        usageLog.serviceType,
                        usageLog.amount,
                        usageLog.tokenUsage,
                        usageLog.balanceAfter,
                        usageLog.requestTraceId,
                        usageLog.targetType,
                        usageLog.targetId,
                        usageLog.description,
                        usageLog.createdAt
                ))
                .from(usageLog)
                .orderBy(usageLog.createdAt.desc())
                .limit(5)
                .fetch();
    }
}