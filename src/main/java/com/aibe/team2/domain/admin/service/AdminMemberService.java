package com.aibe.team2.domain.admin.service;

import com.aibe.team2.domain.admin.dto.request.AdminMemberSearchCond;
import com.aibe.team2.domain.admin.dto.response.AdminMemberRow;
import com.aibe.team2.domain.mypage.entity.Member;
import com.aibe.team2.domain.mypage.entity.enums.MemberStatus;
import com.aibe.team2.domain.mypage.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMemberService {

    private final MemberRepository memberRepository;

    public Page<AdminMemberRow> searchMembers(AdminMemberSearchCond cond, Pageable pageable) {
        return memberRepository.searchAdminMembers(cond, pageable);
    }

    @Transactional
    public MemberStatus updateMemberStatus(Long memberId, MemberStatus status) {
        Member member = memberRepository.getByIdThrowForUpdate(memberId); // 네가 이미 추가한 락 메서드
        member.updateStatus(status);
        return member.getStatus();
    }
}