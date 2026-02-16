package com.aibe.team2.domain.mypage.repository;

import com.aibe.team2.domain.mypage.entity.Member;
import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.custom.NotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member,Long> {

    // 1. 이메일로 회원 찾기
    Optional<Member> findByEmail(String email);

    // 2. 닉네임으로 회원 찾기(닉네임 중복 검사 시 사용)
    Optional<Member> findByNickname(String nickname);

    // 3. ID로 멤버 찾기(없으면 예외 발생)
    default Member getByIdThrow(Long memberId) {
        return findById(memberId)
                .orElseThrow(()-> new NotFoundException(ErrorCode.USER_NOT_FOUND));
    }
}
