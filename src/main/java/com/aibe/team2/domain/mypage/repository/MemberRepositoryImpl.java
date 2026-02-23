package com.aibe.team2.domain.mypage.repository;

import com.aibe.team2.domain.mypage.entity.Member;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

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
                .where(member.id.eq(memberId)) // 조건 : ID가 일치하는 직원
                .fetchOne(); // 단건 조회

        return Optional.ofNullable(findMember);
    }
}
