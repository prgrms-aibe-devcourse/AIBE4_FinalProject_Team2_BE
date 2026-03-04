package com.aibe.team2.domain.mypage.controller;

import com.aibe.team2.domain.file.service.S3ImageService;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/mypage")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // TODO : 보안 연동
    // Spring Security 생성 전 PE와 통신 테스트를 위해 임시로 ID를 1L로 고정
    // 이후 로그인 파트가 완성되면 @AuthenticationPrincipal 어노테이션으로 실제 유저 ID를 받아오도록 수정 필요
    private Long getLoginMemberId(){
        return 1L; // 항상 회원의 정보가 수정되도록 임시 고정
    }

    // [FR-MYP-05] 프로필 조회
    @GetMapping("/profile")
    public ResponseEntity<MemberResponse> getProfile(){
        Long memberId = getLoginMemberId();
        MemberResponse response = memberService.getMemberInfo(memberId);
        return ResponseEntity.ok(response);
    }

    // [FR-MYP-01] 프로필 수정
    @PatchMapping("/profile")
    public ResponseEntity<ApiResponse<MemberUpdateResponse>> updateProfile( // 제네릭 타입 변경
                                                                            @Valid @RequestBody ProfileUpdate dto
    ){
        Long memberId = getLoginMemberId();

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
            @RequestPart("file") MultipartFile file
    ) {
        Long memberId = getLoginMemberId();

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
            @Valid @RequestBody PasswordChangeRequest dto
    ){
        Long memberId = getLoginMemberId();
        memberService.changePassword(memberId, dto);

        PasswordChangeResponse response = new PasswordChangeResponse(
                200,
                "비밀번호가 성공적으로 변경되었습니다."
        );

        return ResponseEntity.ok(response);
    }
}
