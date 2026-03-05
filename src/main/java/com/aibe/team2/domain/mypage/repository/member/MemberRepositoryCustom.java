package com.aibe.team2.domain.mypage.repository.member;

import com.aibe.team2.domain.mypage.entity.Member;
import com.aibe.team2.domain.admin.dto.request.AdminMemberSearchCond;
import com.aibe.team2.domain.admin.dto.response.AdminMemberRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface MemberRepositoryCustom {
    // 테스트용 : QueryDSL을 사용해 프로필 ID로 회원 찾기
    Optional<Member> findProfileByIdWithQueryDSL(Long memberId);

    // 관리자용 회원 목록 검색/페이징
    Page<AdminMemberRow> searchAdminMembers(AdminMemberSearchCond cond, Pageable pageable);
}
