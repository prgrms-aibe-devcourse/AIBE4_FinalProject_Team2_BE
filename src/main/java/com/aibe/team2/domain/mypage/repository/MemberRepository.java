package com.aibe.team2.domain.mypage.repository;

import com.aibe.team2.domain.mypage.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member,Long> {

    // 1. 이메일로 회원 찾기
    Optional<Member> findByEmail(String email);

    // 2. 닉네임으로 회원 찾기(닉네임 중복 검사 시 사용)
    Optional<Member> findByNickname(String nickname);
}
