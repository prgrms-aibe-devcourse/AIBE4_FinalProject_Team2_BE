package com.aibe.team2.domain.auth.controller;

import com.aibe.team2.domain.auth.dto.EmailRequest;
import com.aibe.team2.domain.auth.dto.EmailVerifyRequest;
import com.aibe.team2.domain.auth.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

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
    public ResponseEntity<String> requestVerification(@RequestBody EmailRequest request) {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body("이메일 주소는 필수입니다.");
        }

        emailService.sendVerificationCode(email);
        return ResponseEntity.ok("인증 번호가 발송되었습니다. 5분 이내에 입력해주세요.");
    }

    /**
     * 2. 인증 번호 확인
     * POST /api/v1/auth/email/verify
     */
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyCode(@RequestBody EmailVerifyRequest request) {
        String email = request.getEmail();
        String code = request.getCode();

        boolean isVerified = emailService.verifyCode(email, code);

        if (isVerified) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "이메일 인증에 성공했습니다."
            ));
        } else {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "인증 번호가 일치하지 않거나 만료되었습니다."
            ));
        }
    }
}