package com.aibe.team2.domain.mypage.repository.member;

import com.aibe.team2.domain.admin.dto.request.AdminMemberSearchCond;
import com.aibe.team2.domain.admin.dto.response.AdminMemberRow;
import com.aibe.team2.domain.mypage.entity.Member;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.aibe.team2.domain.mypage.entity.QMember.member;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Member> findProfileByIdWithQueryDSL(Long memberId) {
        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.memberId.eq(memberId)) // 조건 : ID가 일치하는 직원
                .fetchOne(); // 단건 조회

        return Optional.ofNullable(findMember);
    }

    @Override
    public Page<AdminMemberRow> searchAdminMembers(AdminMemberSearchCond cond, Pageable pageable) {
        BooleanBuilder where = new BooleanBuilder();

        if (cond != null) {
            if (cond.getMemberId() != null) {
                where.and(member.memberId.eq(cond.getMemberId()));
            }
            if (cond.getEmail() != null && !cond.getEmail().isBlank()) {
                where.and(member.email.containsIgnoreCase(cond.getEmail()));
            }
            if (cond.getNickname() != null && !cond.getNickname().isBlank()) {
                where.and(member.nickname.containsIgnoreCase(cond.getNickname()));
            }
            if (cond.getStatus() != null) {
                where.and(member.status.eq(cond.getStatus()));
            }
        }

        List<AdminMemberRow> content = queryFactory
                .select(Projections.constructor(
                        AdminMemberRow.class,
                        member.memberId,
                        member.email,
                        member.nickname,
                        member.role,
                        member.status,
                        member.creditBalance,
                        member.createdAt,
                        member.deletedAt
                ))
                .from(member)
                .where(where)
                .orderBy(member.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(member.count())
                .from(member)
                .where(where)
                .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }
}
