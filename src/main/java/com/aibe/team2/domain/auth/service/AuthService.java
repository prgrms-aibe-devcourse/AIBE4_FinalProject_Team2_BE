package com.aibe.team2.domain.auth.service;

import com.aibe.team2.domain.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public void logout(String username) {
        // Redis에 저장된 RefreshToken 삭제
        refreshTokenRepository.deleteById(username);
    }
}