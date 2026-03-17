package com.aibe.team2.domain.error.repository;

import com.aibe.team2.domain.error.dto.admin.ErrorIssueSearchCond;
import com.aibe.team2.domain.error.entity.ErrorIssue;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.aibe.team2.domain.error.entity.QErrorIssue.errorIssue;

@RequiredArgsConstructor
public class ErrorIssueRepositoryImpl implements ErrorIssueRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ErrorIssue> search(ErrorIssueSearchCond cond, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        if (cond != null) {
            if (cond.getStatus() != null) {
                builder.and(errorIssue.status.eq(cond.getStatus()));
            }
            if (cond.getSeverity() != null) {
                builder.and(errorIssue.severity.eq(cond.getSeverity()));
            }
            if (cond.getErrorDomain() != null) {
                builder.and(errorIssue.errorDomain.eq(cond.getErrorDomain()));
            }
        }

        List<ErrorIssue> content = queryFactory
                .selectFrom(errorIssue)
                .where(builder)
                .orderBy(errorIssue.lastOccurredAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(errorIssue.count())
                .from(errorIssue)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0L : total);
    }
}