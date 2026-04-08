package com.aibe.team2.domain.mypage.repository.member;

import com.aibe.team2.domain.mypage.entity.Member;
import com.aibe.team2.domain.mypage.entity.enums.MemberStatus;
import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.custom.NotFoundException;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member,Long>, MemberRepositoryCustom {

    // 1. 이메일로 회원 찾기
    Optional<Member> findByEmail(String email);

    // 2. 닉네임으로 회원 찾기(닉네임 중복 검사 시 사용)
    Optional<Member> findByNickname(String nickname);

    long countByStatus(MemberStatus status);

    // 3. ID로 멤버 찾기(없으면 예외 발생)
    default Member getByIdThrow(Long memberId) {
        return findById(memberId)
                .orElseThrow(()-> new NotFoundException(ErrorCode.USER_NOT_FOUND));
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Member m where m.memberId = :memberId")
    Optional<Member> findByIdForUpdate(@Param("memberId") Long memberId);

    default Member getByIdThrowForUpdate(Long memberId) {
        return findByIdForUpdate(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));
    }

    // 닉네임(아이디) 중복 확인
    boolean existsByNickname(String nickname);

    // 이메일 중복 확인
    boolean existsByEmail(String email);

    // 상태가 ACTIVE이면서 lastLoginAt이 특정 시점보다 이전인 멤버 찾기
    List<Member> findAllByStatusAndLastLoginAtBefore(MemberStatus status, LocalDateTime dateTime);
}
