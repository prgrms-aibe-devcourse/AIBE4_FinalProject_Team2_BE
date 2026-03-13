package com.aibe.team2.domain.auth.controller;

import com.aibe.team2.domain.auth.dto.CustomUserDetails;
import com.aibe.team2.domain.auth.dto.LoginRequest;
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
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {

        try {
            String email = request.getEmail();
            String password = request.getPassword();

            // 1. 인증 시도
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            // 2. 인증 객체에서 사용자 정보 꺼내기
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Member member = userDetails.member();

            // 3. 토큰 생성
            String accessToken = jwtTokenProvider.createAccessToken(member.getEmail(), member.getRole().name());
            String refreshToken = jwtTokenProvider.createRefreshToken(member.getEmail(), member.getRole().name());

            // 4. 응답 반환
            return ResponseEntity.ok(
                    new com.aibe.team2.domain.auth.dto.response.LoginResponse(
                            accessToken,
                            refreshToken,
                            member.getRole().name(),
                            member.getEmail(),
                            member.getNickname()
                    )
            );

        } catch (AuthenticationException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("이메일 또는 비밀번호가 틀렸습니다.");
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
    public ResponseEntity<?> reissue(
            @CookieValue(value = "refreshToken", required = false) String refreshToken
    ) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh Token이 없습니다.");
        }

        // 1. Refresh Token 유효성 검사
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh Token이 만료되었습니다.");
        }

        // 2. Redis에서 해당 토큰이 존재하는지 확인
        String email = jwtTokenProvider.getEmail(refreshToken);

        RefreshToken savedToken = refreshTokenRepository.findById(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (!savedToken.getRefreshToken().equals(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("토큰 정보가 일치하지 않습니다.");
        }

        // 3. 새로운 Access Token 발급
        String role = jwtTokenProvider.getRole(refreshToken);
        String newAccessToken = jwtTokenProvider.createAccessToken(email, role);

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