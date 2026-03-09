package com.aibe.team2.domain.auth.controller;

import com.aibe.team2.domain.auth.service.EmailService;
import com.aibe.team2.domain.mypage.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;
    private final MemberRepository memberRepository;

    // 1. 인증 코드 발송
    @PostMapping("/request")
    public ResponseEntity<?> sendMessage(@RequestParam String email) {
        if (memberRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body("이미 가입된 이메일입니다.");
        }
        emailService.sendVerificationCode(email);
        return ResponseEntity.ok("인증 코드가 발송되었습니다.");
    }

    // 2. 코드 검증
    @PostMapping("/verify")
    public ResponseEntity<?> verifyCode(@RequestParam String email, @RequestParam String code) {
        if (emailService.verifyCode(email, code)) {
            return ResponseEntity.ok("인증에 성공하였습니다.");
        }
        return ResponseEntity.badRequest().body("인증 코드가 일치하지 않거나 만료되었습니다.");
    }
}