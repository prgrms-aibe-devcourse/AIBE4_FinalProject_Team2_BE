package com.aibe.team2.domain.auth.controller;

import com.aibe.team2.domain.auth.dto.EmailRequest;
import com.aibe.team2.domain.auth.dto.EmailVerifyRequest;
import com.aibe.team2.domain.auth.service.EmailService;
import com.aibe.team2.global.common.response.ApiResponse;
import com.aibe.team2.global.common.response.ErrorResponse;
import com.aibe.team2.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    /**
     * 1. 인증 번호 발송
     * POST /api/v1/auth/email/request
     */
    @PostMapping("/request")
    public ResponseEntity<?> requestVerification(@RequestBody EmailRequest request) {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return ErrorResponse.toResponseEntity(ErrorCode.AUTH_EMAIL_NOT_FOUND);
        }

        emailService.sendVerificationCode(email);
        return ResponseEntity.ok(ApiResponse.success("인증 번호가 발송되었습니다. 5분 이내에 입력해주세요."));
    }

    /**
     * 2. 인증 번호 확인
     * POST /api/v1/auth/email/verify
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyCode(@RequestBody EmailVerifyRequest request) {
        String email = request.getEmail();
        String code = request.getCode();

        boolean isVerified = emailService.verifyCode(email, code);

        if (isVerified) {
            return ResponseEntity.ok(ApiResponse.success("이메일 인증에 성공했습니다."));
        } else {
            return ErrorResponse.toResponseEntity(ErrorCode.AUTH_VERIFICATION_FAILED);
        }
    }
}