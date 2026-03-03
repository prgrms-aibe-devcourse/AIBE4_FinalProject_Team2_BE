package com.aibe.team2.domain.auth.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private final String secretKey = "your-very-long-secret-key-for-jwt-security"; // 보안상 외부에 노출 금지
    private final long tokenValidityInMilliseconds = 3600000; // 1시간

    // 토큰 생성
    public String createToken(Authentication auth) {
        Claims claims = Jwts.claims().setSubject(auth.getName());
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + tokenValidityInMilliseconds))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // 토큰에서 인증 정보 조회
    public Authentication getAuthentication(String token) {
        String username = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
        // 실제 DB 조회 로직이 필요할 수 있음
        return new UsernamePasswordAuthenticationToken(username, "", List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }
}