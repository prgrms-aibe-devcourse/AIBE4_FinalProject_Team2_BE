package com.aibe.team2.domain.mypage.service;

import com.aibe.team2.domain.auth.dto.MemberDTO;
import com.aibe.team2.domain.file.service.S3ImageService;
import com.aibe.team2.domain.mypage.dto.request.JobPreferenceUpdateRequest;
import com.aibe.team2.domain.mypage.dto.request.PasswordChangeRequest;
import com.aibe.team2.domain.mypage.dto.request.ProfileUpdate;
import com.aibe.team2.domain.mypage.dto.response.MemberResponse;
import com.aibe.team2.domain.mypage.dto.response.MemberUpdateResponse;
import com.aibe.team2.domain.mypage.entity.Member;
import com.aibe.team2.domain.mypage.entity.enums.Role;
import com.aibe.team2.domain.mypage.repository.member.MemberRepository;
import com.aibe.team2.domain.statistics.enums.ServiceType;
import com.aibe.team2.domain.statistics.service.usage.UsageLogWriter;
import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.BusinessException;
import com.aibe.team2.global.exception.custom.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsageLogWriter usageLogWriter;
    private final S3ImageService s3ImageService;

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
            applyJobPreferencesLogic(member, dto.getJobPreferences());
        }

        // 2-3. 수정이 완료된 엔티티를 Response DTO로 변환하여 반환
        return new MemberUpdateResponse(member);
    }

    // 3. 비밀번호 변경
    @Transactional
    public void changePassword(Long memberId, PasswordChangeRequest dto){
        Member member = memberRepository.getByIdThrow(memberId);

        // 3-1. 현재 비밀번호 검증
        if (!passwordEncoder.matches(dto.getCurrentPassword(), member.getPassword())) {
                    throw new BadRequestException(ErrorCode.USER_PASSWORD_MISMATCH);
                }

        // 3-2. 새 비밀번호 일치 검증
        if(!dto.getNewPassword().equals(dto.getConfirmPassword())){
            throw new BadRequestException(ErrorCode.USER_PASSWORD_MISMATCH);
        }

        // 3-3. 비밀번호 업데이트
        String encodedNewPassword = passwordEncoder.encode(dto.getNewPassword());
                member.updatePassword(encodedNewPassword);
    }

    private void applyJobPreferencesLogic(Member member, JobPreferenceUpdateRequest dto) {
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

    @Transactional
    public String updateProfileImage(Long memberId, MultipartFile file) {
        // 1. 회원 조회
        Member member = memberRepository.getByIdThrow(memberId);

        // 삭제할 기존 이미지 URL을 미리 저장
        String oldImageUrl = member.getProfileImageUrl();

        // 2. 새 이미지 S3 업로드
        String newImageUrl = s3ImageService.uploadProfileImage(file, memberId);

        // 3. DB 엔티티의 profileImageUrl 필드 업데이트
        member.updateProfileImage(newImageUrl);

        // 4. 기존 이미지가 존재했다면 S3에서 삭제(새 이미지 업로드 및 DB 반영이 정상 진행된 후 실행)
        if(oldImageUrl != null && !oldImageUrl.isBlank()) {
            s3ImageService.deleteImage(oldImageUrl);
        }

        return newImageUrl;
    }

    public void validateSignup(MemberDTO request) {
        // 1. 닉네임 중복 검사
        if (memberRepository.existsByNickname(request.getNickname())) {
            throw new BusinessException(ErrorCode.AUTH_DUPLICATE_NICKNAME);
        }

        // 2. 이메일 중복 검사
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.AUTH_DUPLICATE_EMAIL);
        }
    }

    @Transactional
    public void join(MemberDTO request) {
        validateSignup(request); // 가입 전 검증 수행

        Member member = Member.builder()
                .nickname(request.getNickname())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .role(Role.MEMBER)
                .build();

        memberRepository.save(member);
    }
}
