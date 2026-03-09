package com.aibe.team2.domain.auth.controller;

import com.aibe.team2.domain.auth.dto.MemberDTO;
import com.aibe.team2.domain.auth.repository.RefreshTokenRepository;
import com.aibe.team2.domain.auth.service.AuthService;
import com.aibe.team2.domain.auth.util.JwtTokenProvider;
import com.aibe.team2.domain.auth.util.RefreshToken;
import com.aibe.team2.domain.mypage.entity.Member;
import com.aibe.team2.domain.mypage.repository.member.MemberRepository;
import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> user) {
        try {
            // 1. 아이디/비번으로 인증 시도
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.get("username"), user.get("password"))
            );

            // 2. 인증 성공 시 토큰 생성
            String token = jwtTokenProvider.createAccessToken(user.get("username"));
            return ResponseEntity.ok(Collections.singletonMap("token", token));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디 또는 비밀번호가 틀렸습니다.");
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody MemberDTO request) {

        // 1 - 1. 중복 사용자 확인
        if (memberRepository.existsByNickname(request.getNickname())) {
            return ResponseEntity.badRequest().body("이미 존재하는 아이디입니다.");
        }

        // 1 - 2. 중복 이메일 확인
        if (memberRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("이미 존재하는 이메일입니다.");
        }

        // 2. 사용자 생성 및 비밀번호 암호화
        Member member = new Member(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getNickname(),
                null,
                request.getProvider()
        );

        memberRepository.save(member);
        return ResponseEntity.ok("회원가입이 완료되었습니다.");
    }

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        // 1. Refresh Token 유효성 검사
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(401).body("Refresh Token이 만료되었습니다.");
        }

        // 2. Redis에서 해당 토큰이 존재하는지 확인
        String username = jwtTokenProvider.getUsername(refreshToken);
        RefreshToken savedToken = refreshTokenRepository.findById(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!savedToken.getRefreshToken().equals(refreshToken)) {
            return ResponseEntity.status(401).body("토큰 정보가 일치하지 않습니다.");
        }

        // 3. 새로운 Access Token 발급
        String newAccessToken = jwtTokenProvider.createAccessToken(username);
        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            authService.logout(authentication.getName());
            return ResponseEntity.ok("로그아웃 되었습니다.");
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인 상태가 아닙니다.");
    }
}