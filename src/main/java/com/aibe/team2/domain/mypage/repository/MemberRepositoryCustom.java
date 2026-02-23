package com.aibe.team2.domain.mypage.repository;

import com.aibe.team2.domain.mypage.entity.Member;

import java.util.Optional;

public interface MemberRepositoryCustom {
    // 테스트용 : QueryDSL을 사용해 프로필 ID로 회원 찾기
    Optional<Member> findProfileByIdWithQueryDSL(Long memberId);
}
