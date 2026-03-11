package com.aibe.team2.domain.auth.util;

import com.aibe.team2.domain.auth.repository.RefreshTokenRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
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

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

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
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + exp))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 토큰에서 사용자 아이디(Subject) 추출
    public String getUsername(String token) {
        return Jwts.parser()                    // 1. parserBuilder() 대신 parser() 사용
                .verifyWith(key)               // 2. setSigningKey() 대신 verifyWith()
                .build()
                .parseSignedClaims(token)      // 3. parseClaimsJws() 대신 parseSignedClaims()
                .getPayload()                  // 4. getBody() 대신 getPayload()
                .getSubject();
    }

    // 토큰 유효성 검사
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);     // 서명이나 만료 시간에 문제가 있으면 예외 발생
            return true;
        } catch (io.jsonwebtoken.security.SignatureException e) {
            System.out.println("잘못된 JWT 서명입니다.");
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            System.out.println("만료된 JWT 토큰입니다.");
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            System.out.println("지원되지 않는 JWT 토큰입니다.");
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            System.out.println("구조가 잘못된 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            System.out.println("JWT 토큰이 비어있거나 잘못되었습니다.");
        }
        return false;
    }
}