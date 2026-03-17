package com.aibe.team2.domain.auth.service;

import com.aibe.team2.domain.mypage.repository.member.MemberRepository;
import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate; // Redis 사용
    private final MemberRepository memberRepository;

    public void sendVerificationCode(String email) {
        // 0. 이메일 중복 확인
        if (memberRepository.existsByEmail(email)) {
            // 커스텀 예외를 던지거나, 간단하게 RuntimeException 활용
            throw new BusinessException(ErrorCode.AUTH_DUPLICATE_EMAIL);
        }

        // 1. 6자리 난수 생성
        String code = String.valueOf((int)(Math.random() * 899999) + 100000);

        // 2. Redis에 저장 (Key: 이메일, Value: 코드, 유효시간: 5분)
        redisTemplate.opsForValue().set(email, code, Duration.ofMinutes(5));

        // 3. 메일 발송
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[서비스명] 회원가입 인증 코드입니다.");
        message.setText("인증 코드: " + code);
        mailSender.send(message);
    }

    public boolean verifyCode(String email, String code) {
        String savedCode = redisTemplate.opsForValue().get(email);
        redisTemplate.delete(email);
        return code.equals(savedCode);
    }
}