package com.aibe.team2.domain.auth.service;

import com.aibe.team2.domain.auth.repository.RefreshTokenRepository;
import com.aibe.team2.domain.mypage.entity.Member;
import com.aibe.team2.domain.mypage.repository.member.MemberRepository;
import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.BusinessException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final MemberRepository memberRepository;

    @Transactional
    public void logout(String username) {
        // Redis에 저장된 RefreshToken 삭제
        refreshTokenRepository.deleteById(username);
    }

    public void handleSingleDeviceLogin(String email) {
        String redisKey = "RT:" + email;
        String existingToken = redisTemplate.opsForValue().get(redisKey);

        if (existingToken != null) {
            // 1. 기존 기기에 알림 전송 (WebSocket)
            messagingTemplate.convertAndSend("/topic/logout/" + email,
                    "새로운 기기에서 로그인했습니다. 접속을 종료합니다.");

            // 2. 기존 Refresh Token 삭제
            redisTemplate.delete(redisKey);
        }
    }

    @Transactional
    public void updateLoginTime(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        member.updateLastLoginAt();
    }
}