package com.aibe.team2.domain.mypage.controller;

import com.aibe.team2.domain.mypage.dto.response.MemberResponse;
import com.aibe.team2.domain.mypage.dto.response.MemberUpdateResponse;
import com.aibe.team2.domain.mypage.dto.request.PasswordChangeRequest;
import com.aibe.team2.domain.mypage.dto.request.ProfileUpdate;
import com.aibe.team2.domain.mypage.dto.response.PasswordChangeResponse;
import com.aibe.team2.domain.mypage.service.MemberService;
import com.aibe.team2.global.common.annotation.LoginMemberId;
import com.aibe.team2.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/mypage")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // [FR-MYP-05] 프로필 조회
    @GetMapping("/profile")
    public ResponseEntity<MemberResponse> getProfile(
            @LoginMemberId Long memberId
    ){
        MemberResponse response = memberService.getMemberInfo(memberId);
        return ResponseEntity.ok(response);
    }

    // [FR-MYP-01] 프로필 수정
    @PatchMapping("/profile")
    public ResponseEntity<ApiResponse<MemberUpdateResponse>> updateProfile(
            @LoginMemberId Long memberId,
            @Valid @RequestBody ProfileUpdate dto
    ){
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
            @LoginMemberId Long memberId,
            @RequestPart("file") MultipartFile file
    ) {
        String uploadedUrl = memberService.updateProfileImage(memberId, file);

        ApiResponse<String> apiResponse = ApiResponse.<String> builder()
                .success(true)
                .code("OK")
                .message("프로필 이미지 수정이 완료되었습니다.")
                .data(uploadedUrl)
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    // 프로필 이미지 삭제
    @DeleteMapping("/profile/image")
    public ResponseEntity<ApiResponse<Void>> deleteProfileImage(
            @LoginMemberId Long memberId
    ) {
        // MemberService에 이미지 삭제 로직을 위임합니다.
        memberService.deleteProfileImage(memberId);

        ApiResponse<Void> apiResponse = ApiResponse.<Void> builder()
                .success(true)
                .code("OK")
                .message("프로필 이미지가 기본 이미지로 변경되었습니다.")
                .build();

        return ResponseEntity.ok(apiResponse);
    }

    // [FR-MYP-02] 비밀번호 변경
    @PatchMapping("/password")
    public ResponseEntity<PasswordChangeResponse> changePassword(
            @LoginMemberId Long memberId,
            @Valid @RequestBody PasswordChangeRequest dto
    ){
        memberService.changePassword(memberId, dto);

        PasswordChangeResponse response = new PasswordChangeResponse(
                200,
                "비밀번호가 성공적으로 변경되었습니다."
        );

        return ResponseEntity.ok(response);
    }
}
