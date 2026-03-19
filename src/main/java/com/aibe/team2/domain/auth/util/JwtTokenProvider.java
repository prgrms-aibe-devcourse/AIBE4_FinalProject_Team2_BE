package com.aibe.team2.domain.auth.util;

import com.aibe.team2.domain.auth.entity.RefreshToken;
import com.aibe.team2.domain.auth.repository.RefreshTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.secret}") private String secret;
    @Value("${jwt.access-token-validity}") private long accessTokenValidity;
    @Value("${jwt.refresh-token-validity}") private long refreshTokenValidity;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Access Token 생성
    public String createAccessToken(String email, String role) {
        return createToken(email, role, accessTokenValidity);
    }

    // Refresh Token 생성 및 Redis 저장
    public String createRefreshToken(String email, String role) {
        String token = createToken(email, role, refreshTokenValidity);
        refreshTokenRepository.save(new RefreshToken(email, token));
        return token;
    }

    // 토큰 생성
    public String createToken(String email, String role, long exp) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + exp))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getEmail(String token) {
        return getClaims(token).getSubject();
    }

    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody();
    }

    // 토큰 유효성 검사
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .setSigningKey(key)
                    .parseClaimsJws(token);   // 서명이나 만료 시간에 문제가 있으면 예외 발생
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