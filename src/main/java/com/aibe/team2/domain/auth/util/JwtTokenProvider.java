package com.aibe.team2.domain.auth.util;

import com.aibe.team2.domain.auth.repository.RefreshTokenRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.secret}") private String secret;
    @Value("${jwt.access-token-validity}") private long accessTokenValidity;
    @Value("${jwt.refresh-token-validity}") private long refreshTokenValidity;
    private final Key key = Keys.hmacShaKeyFor(secret.getBytes());

    // Access Token 생성
    public String createAccessToken(String username) {
        return createToken(username, accessTokenValidity);
    }

    // Refresh Token 생성 및 Redis 저장
    public String createRefreshToken(String username) {
        String token = createToken(username, refreshTokenValidity);
        refreshTokenRepository.save(new RefreshToken(username, token));
        return token;
    }

    // 토큰 생성
    public String createToken(String username, long exp) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + exp))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰에서 사용자 아이디 추출
    public String getUsername(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }

    // 토큰 유효성 검사
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}