package com.aibe.team2.domain.auth.service;

import com.aibe.team2.domain.mypage.repository.member.MemberRepository;
import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.BusinessException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate; // Redis 사용
    private final MemberRepository memberRepository;

    @Async
    public void sendVerificationCode(String email) {
        // 0. 이메일 중복 확인
        if (memberRepository.existsByEmail(email)) {
            // 커스텀 예외를 던지거나, 간단하게 RuntimeException 활용
            throw new BusinessException(ErrorCode.AUTH_DUPLICATE_EMAIL);
        }

        // 1. 6자리 난수 생성
        String code = String.valueOf((int) (Math.random() * 899999) + 100000);

        try {

            // 2. Redis에 저장 (Key: 이메일, Value: 코드, 유효시간: 5분)
            redisTemplate.opsForValue().set(email, code, Duration.ofMinutes(5));
            log.info("Redis에 인증 코드 임시 저장 완료: {}", email);

            // 3. 메일 발송
            MimeMessage message = mailSender.createMimeMessage();
            // true는 멀티파트 메시지(HTML + 이미지 등)를 지원한다는 의미입니다.
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("[SyncTalk] 회원가입 인증 코드입니다.");

            // HTML 디자인 템플릿 구성
            String htmlContent =
                    "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #e0e0e0; border-radius: 10px; overflow: hidden;'>" +
                            "<div style='background-color: #1976D2; padding: 20px; text-align: center;'>" +
                            "<h1 style='color: white; margin: 0; font-size: 24px;'>SyncTalk</h1>" +
                            "</div>" +
                            "<div style='padding: 40px 20px; text-align: center; color: #333;'>" +
                            "<h2 style='margin-bottom: 20px;'>환영합니다!</h2>" +
                            "<p style='font-size: 16px; color: #666;'>꿈의 직장으로 가는 첫 걸음, SyncTalk과 함께하세요.<br/>아래의 인증 코드를 회원가입 화면에 입력해주세요.</p>" +
                            "<div style='margin: 30px auto; padding: 20px; background-color: #f8f9fa; border-radius: 8px; border: 1px dashed #1976D2; display: inline-block;'>" +
                            "<span style='font-size: 32px; font-weight: bold; color: #1976D2; letter-spacing: 5px;'>" + code + "</span>" +
                            "</div>" +
                            "<p style='font-size: 14px; color: #999; margin-top: 20px;'>인증 코드는 5분 동안 유효합니다.</p>" +
                            "</div>" +
                            "<div style='background-color: #f1f1f1; padding: 20px; text-align: center; font-size: 12px; color: #888;'>" +
                            "본 메일은 발신 전용입니다. 문의사항은 고객센터를 이용해주세요.<br/>" +
                            "© 2026 SyncTalk Team 2. All rights reserved." +
                            "</div>" +
                            "</div>";

            // true를 설정해야 HTML로 렌더링됩니다.
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("비동기 메일 발송 완료: {}", email);

        } catch (MessagingException e) {
            // 이메일 설정 오류나 서버 연결 실패 시 발생
            // [핵심] 발송 실패 시 Redis 데이터 삭제 (Rollback)
            redisTemplate.delete(email);
            log.error("메일 발송 실패로 인해 Redis 데이터를 롤백합니다. 대상: {}, 사유: {}", email, e.getMessage());
            log.error("메일 전송 중 오류 발생: {}", e.getMessage());
            throw new BusinessException(ErrorCode.AUTH_EMAIL_SEND_ERROR);
        } catch (Exception e) {
            // 그 외 예상치 못한 시스템 오류
            log.error("서버 내부 오류: {}", e.getMessage());
            throw new BusinessException(ErrorCode.COMMON_500);
        }
    }

    public boolean verifyCode(String email, String code) {
        String savedCode = redisTemplate.opsForValue().get(email);
        redisTemplate.delete(email);
        return code.equals(savedCode);
    }
}