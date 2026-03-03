package com.aibe.team2.domain.mypage.service;

import com.aibe.team2.domain.mypage.dto.request.JobPreferenceUpdateRequest;
import com.aibe.team2.domain.mypage.dto.request.PasswordChangeRequest;
import com.aibe.team2.domain.mypage.dto.request.ProfileUpdate;
import com.aibe.team2.domain.mypage.dto.response.MemberResponse;
import com.aibe.team2.domain.mypage.dto.response.MemberUpdateResponse;
import com.aibe.team2.domain.mypage.entity.Member;
import com.aibe.team2.domain.mypage.repository.member.MemberRepository;
import com.aibe.team2.domain.statistics.enums.ServiceType;
import com.aibe.team2.domain.statistics.service.usage.UsageLogWriter;
import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.custom.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    // TODO : Security Config에 PasswordEncoder Bean 등록 시 주석 풀기
    // private final PasswordEncoder passwordEncoder;
    private final UsageLogWriter usageLogWriter;

    // 1. 마이페이지 정보 조회
    public MemberResponse getMemberInfo(Long memberId){
        Member member = memberRepository.getByIdThrow(memberId);
        return new MemberResponse(member);
    }

    // 2. 프로필 수정 (통합 메서드)
    @Transactional
    public MemberUpdateResponse updateProfile (Long memberId, ProfileUpdate dto) {
        Member member = memberRepository.getByIdThrow(memberId);

        // 2-1. 프로필 기본 정보 업데이트
        member.updateProfile(dto.getNickname(), dto.getProfileImageUrl());

        // 2-2. 취업 선호 설정이 포함되어 있다면 기존 로직(4번) 재사용
        if (dto.getJobPreferences() != null) {
            // 내부 메서드를 호출하여 로직 중복 방지 (DRY 원칙)
            this.updateJobPreferences(memberId, dto.getJobPreferences());
        }

        // 2-3. 수정이 완료된 엔티티를 Response DTO로 변환하여 반환
        return new MemberUpdateResponse(member);
    }

    // 3. 비밀번호 변경
    @Transactional
    public void changePassword(Long memberId, PasswordChangeRequest dto){
        Member member = memberRepository.getByIdThrow(memberId);

        // 3-1. 현재 비밀번호 검증
        // TODO : [삭제] 임시 평문 비교
        if(!dto.getCurrentPassword().equals(member.getPassword())){
            throw new BadRequestException(ErrorCode.COMMON_400);
        }
        // TODO : [주석 해제] 최종 암호화 비교
        // if (!passwordEncoder.matches(dto.getCurrentPassword(), member.getPassword())) {
        //             throw new BadRequestException(ErrorCode.COMMON_400);
        //         }

        // 3-2. 새 비밀번호 일치 검증
        if(!dto.getNewPassword().equals(dto.getConfirmPassword())){
            throw new BadRequestException(ErrorCode.COMMON_400);
        }

        // 3-3. 비밀번호 업데이트
        // TODO : [삭제] 임시 평문 저장
        member.updatePassword(dto.getNewPassword());
        // TODO : [주석 해제] 최종 암호화 저장
        // String encodedNewPassword = passwordEncoder.encode(dto.getNewPassword());
        //         member.updatePassword(encodedNewPassword);
    }

    // 4. 취업 선호 설정 수정
    @Transactional
    public void updateJobPreferences(Long memberId, JobPreferenceUpdateRequest dto){
        Member member = memberRepository.getByIdThrow(memberId);

        List<String> rolesList = dto.getTargetJobRoles();
        String joinedRoles = (rolesList != null && !rolesList.isEmpty())
                ? String.join(",", rolesList)
                : null;

        member.updateJobPreferences(joinedRoles, dto.getPreferredLocation());
    }

    @Transactional
    public int applyCreditDelta(
            Long memberId,
            int tokenDelta,
            ServiceType serviceType,
            String targetType,
            Long targetId,
            String description
    ) {
        Member member = memberRepository.getByIdThrowForUpdate(memberId);

        int before = member.getCreditBalance() == null ? 0 : member.getCreditBalance();
        int after = before + tokenDelta;

        if (after < 0) {
            throw new BadRequestException(ErrorCode.CREDIT_INSUFFICIENT);
        }

        member.updateCreditBalance(after);

        usageLogWriter.record(
                member,
                serviceType,
                1,
                tokenDelta,
                after,
                targetType,
                targetId,
                description
        );

        return after;
    }
}
