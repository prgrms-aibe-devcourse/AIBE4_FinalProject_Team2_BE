package com.aibe.team2.domain.auth.service;

import com.aibe.team2.domain.auth.repository.RefreshTokenRepository;
import com.aibe.team2.domain.mypage.entity.Member;
import com.aibe.team2.domain.mypage.repository.member.MemberRepository;
import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private static final int MAX_ATTEMPTS = 5; // 최대 실패 횟수
    private static final long LOCK_TIME = 5; // 잠금 시간 (분)
    private final SimpMessagingTemplate messagingTemplate;
    private final MemberRepository memberRepository;

    @Transactional
    public void logout(String username) {
        // Redis에 저장된 RefreshToken 삭제
        refreshTokenRepository.deleteById(username);
    }

    // 로그인 시도 전 잠금 여부 확인
    public void checkLoginLock(String email) {
        String lockKey = "lock:" + email;
        if (redisTemplate.hasKey(lockKey)) {
            throw new RuntimeException("잦은 로그인 실패로 인해 계정이 5분간 잠겼습니다.");
        }
    }

    // 로그인 실패 시 횟수 증가
    public void incrementFailureCount(String email) {
        String countKey = "fail_count:" + email;
        Long count = redisTemplate.opsForValue().increment(countKey);

        if (count != null && count >= MAX_ATTEMPTS) {
            // 5회 실패 시 잠금 키 생성 (30분 후 자동 삭제)
            redisTemplate.opsForValue().set("lock:" + email, "locked", Duration.ofMinutes(LOCK_TIME));
            redisTemplate.delete(countKey); // 실패 횟수 초기화
        }
    }

    // 로그인 성공 시 실패 기록 삭제
    public void resetFailureCount(String email) {
        redisTemplate.delete("fail_count:" + email);
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