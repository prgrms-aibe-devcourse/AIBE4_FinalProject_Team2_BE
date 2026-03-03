package com.aibe.team2.domain.admin.service;

import com.aibe.team2.domain.mypage.entity.Member;
import com.aibe.team2.domain.mypage.repository.member.MemberRepository;
import com.aibe.team2.domain.statistics.enums.ServiceType;
import com.aibe.team2.domain.statistics.service.usage.UsageLogWriter;
import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.custom.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminCreditService {

    private final MemberRepository memberRepository;
    private final UsageLogWriter usageLogWriter;

    @Transactional
    public int adjustCreditByAdmin(Long memberId, int tokenDelta, String reason) {

        // 동시 업데이트 방지(락)
        Member member = memberRepository.getByIdThrowForUpdate(memberId);

        int before = member.getCreditBalance() == null ? 0 : member.getCreditBalance();
        int after = before + tokenDelta;

        if (after < 0) {
            throw new BadRequestException(ErrorCode.CREDIT_INSUFFICIENT);
        }

        member.updateCreditBalance(after);

        // 운영 로그
        usageLogWriter.record(
                member,
                ServiceType.ADMIN,
                0,
                tokenDelta,
                after,
                "ADMIN_ADJUST",
                null,
                reason == null ? "관리자 크레딧 조정" : reason
        );

        return after;
    }
}