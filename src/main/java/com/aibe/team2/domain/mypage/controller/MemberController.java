package com.aibe.team2.domain.mypage.controller;

import com.aibe.team2.domain.auth.dto.CustomUserDetails;
import com.aibe.team2.domain.mypage.dto.response.MemberResponse;
import com.aibe.team2.domain.mypage.dto.response.MemberUpdateResponse;
import com.aibe.team2.domain.mypage.dto.request.PasswordChangeRequest;
import com.aibe.team2.domain.mypage.dto.request.ProfileUpdate;
import com.aibe.team2.domain.mypage.dto.response.PasswordChangeResponse;
import com.aibe.team2.domain.mypage.service.MemberService;
import com.aibe.team2.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/mypage")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    private Long getMemberIdWithFallback(CustomUserDetails userDetails) {
        if(userDetails == null) {
            // TODO : 현재 개발 및 테스트 환경을 위한 Fallback ID 반환
            // throw new CustomAuthenticationException("인증된 사용자 정보가 없습니다.")
            return 1L;
        }
        return userDetails.getMember().getMemberId();
    }

    // [FR-MYP-05] 프로필 조회
    @GetMapping("/profile")
    public ResponseEntity<MemberResponse> getProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        Long memberId = getMemberIdWithFallback(userDetails);
        MemberResponse response = memberService.getMemberInfo(memberId);
        return ResponseEntity.ok(response);
    }

    // [FR-MYP-01] 프로필 수정
    @PatchMapping("/profile")
    public ResponseEntity<ApiResponse<MemberUpdateResponse>> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ProfileUpdate dto
    ){
        Long memberId = getMemberIdWithFallback(userDetails);

        // 반환받는 변수의 타입도 신규 DTO로 변경
        MemberUpdateResponse response = memberService.updateProfile(memberId, dto);

        ApiResponse<MemberUpdateResponse> apiResponse = ApiResponse.<MemberUpdateResponse>builder()
                .success(true)
                .code("OK")
                .message("프로필 수정이 완료되었습니다.")
                .data(response)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    // 마이페이지 프로필 이미지 변경
    @PatchMapping(value = "/profile/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<String>> updateProfileImage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("file") MultipartFile file
    ) {
        Long memberId = getMemberIdWithFallback(userDetails);

        String uploadedUrl = memberService.updateProfileImage(memberId, file);

        ApiResponse<String> apiResponse = ApiResponse.<String> builder()
                .success(true)
                .code("OK")
                .message("프로필 이미지 수정이 완료되었습니다.")
                .data(uploadedUrl)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    // [FR-MYP-02] 비밀번호 변경
    @PatchMapping("/password")
    public ResponseEntity<PasswordChangeResponse> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PasswordChangeRequest dto
    ){
        Long memberId = getMemberIdWithFallback(userDetails);
        memberService.changePassword(memberId, dto);

        PasswordChangeResponse response = new PasswordChangeResponse(
                200,
                "비밀번호가 성공적으로 변경되었습니다."
        );

        return ResponseEntity.ok(response);
    }
}
