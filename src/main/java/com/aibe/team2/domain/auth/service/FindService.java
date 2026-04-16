package com.aibe.team2.domain.auth.service;

import com.aibe.team2.domain.mypage.entity.Member;
import com.aibe.team2.domain.mypage.repository.member.MemberRepository;
import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.BusinessException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FindService {

    private final MemberRepository memberRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder;

    // 1. 이메일 찾기
    public String findEmail(String nickname) {
        Member member = memberRepository.findByNickname(nickname)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 보안을 위해 일부 마스킹 처리 (예: user***@gmail.com)
        String email = member.getEmail();
        int atIndex = email.indexOf("@");
        String emailId = email.substring(0, atIndex);
        String maskedId = emailId.length() > 3 ? emailId.substring(0, 3) + "***" : emailId.substring(0, 1) + "***";
        return maskedId + email.substring(atIndex);
    }

    // 2. 비밀번호 찾기 (임시 비밀번호 발송)
    @Transactional
    public void sendTemporaryPassword(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 임시 비밀번호 생성 (8자리 랜덤 문자열)
        String tempPassword = UUID.randomUUID().toString().substring(0, 8);

        // DB 비밀번호 업데이트 (암호화 필수)
        member.updatePassword(passwordEncoder.encode(tempPassword));

        // 메일 발송
        String htmlContent = createTempPasswordHtml(member.getNickname(), tempPassword);
        emailService.sendHtmlMessage(member.getEmail(), "[SyncTalk] 임시 비밀번호 안내", htmlContent);
    }

    private String createTempPasswordHtml(String nickname, String tempPassword) {
        return "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #e0e0e0; border-radius: 10px; overflow: hidden;'>" +
                "<div style='background-color: #1976D2; padding: 20px; text-align: center; color: white;'><h1>SyncTalk</h1></div>" +
                "<div style='padding: 40px 20px; text-align: center;'>" +
                "<h2>임시 비밀번호 발송</h2>" +
                "<p>" + nickname + "님, 요청하신 임시 비밀번호를 발급해 드립니다.</p>" +
                "<div style='background-color: #f8f9fa; padding: 15px; font-size: 24px; font-weight: bold; color: #1976D2; margin: 20px 0;'>" + tempPassword + "</div>" +
                "<p style='color: #d32f2f;'>로그인 후 반드시 비밀번호를 변경해 주세요.</p>" +
                "</div>" +
                "</div>";
    }
}