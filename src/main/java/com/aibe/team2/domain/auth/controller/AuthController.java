package com.aibe.team2.domain.auth.controller;

import com.aibe.team2.domain.auth.dto.LoginRequest;
import com.aibe.team2.domain.auth.dto.LoginResponse;
import com.aibe.team2.domain.auth.dto.MemberDTO;
import com.aibe.team2.domain.auth.repository.RefreshTokenRepository;
import com.aibe.team2.domain.auth.service.AuthService;
import com.aibe.team2.domain.auth.dto.CustomUserDetails;
import com.aibe.team2.domain.auth.util.JwtTokenProvider;
import com.aibe.team2.domain.auth.entity.RefreshToken;
import com.aibe.team2.domain.mypage.entity.Member;
import com.aibe.team2.domain.mypage.entity.enums.Role;
import com.aibe.team2.domain.mypage.repository.member.MemberRepository;
import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.BusinessException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
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
            Member member = userDetails.getMember();

            // 3. 토큰 생성
            String accessToken = jwtTokenProvider.createAccessToken(member.getEmail(), member.getRole().name());
            String refreshToken = jwtTokenProvider.createRefreshToken(member.getEmail(), member.getRole().name());

            // 4. 응답 반환
            return ResponseEntity.ok(
                    new LoginResponse(
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
                Role.MEMBER,
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

    @GetMapping("/logout")
    public ResponseEntity<?> logout(Authentication authentication, HttpServletResponse response) {
        // 1. Redis에서 RefreshToken 삭제 (보안 핵심)
        if (authentication != null) {
            refreshTokenRepository.deleteById(authentication.getName());
        }

        // 2. AccessToken 쿠키 만료 설정
        ResponseCookie deleteAccessCookie = ResponseCookie.from("accessToken", null)
                .path("/")
                .httpOnly(false) // 자바스크립트에서 쿠키에 접근하기 위해 false 설정
                .secure(false) // 로컬 환경에서는 false, https 환경에서는 true
                .sameSite("Lax")
                .maxAge(0) // 즉시 만료
                .build();

        // 3. RefreshToken 쿠키 만료 설정
        ResponseCookie deleteRefreshCookie = ResponseCookie.from("refreshToken", null)
                .path("/")
                .httpOnly(false) // 자바스크립트에서 쿠키에 접근하기 위해 false 설정
                .secure(false) // 로컬 환경에서는 false, https 환경에서는 true
                .sameSite("Lax")
                .maxAge(0) // 즉시 만료
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, deleteAccessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, deleteRefreshCookie.toString());

        // 4. 응답 헤더에 담아 전송
        return ResponseEntity.ok("로그아웃 성공");
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        // 1. 요청의 쿠키에서 accessToken 추출
        Cookie[] cookies = request.getCookies();
        String token = null;
        if (cookies != null) {
            token = Arrays.stream(cookies)
                    .filter(c -> "accessToken".equals(c.getName()))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElse(null);
        }

        if (token == null || !jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 2. 토큰이 유효하면 유저 정보와 함께 토큰을 JSON으로 반환 (프론트가 저장할 수 있도록)
        String email = jwtTokenProvider.getEmail(token);
        Map<String, String> response = new HashMap<>();
        response.put("email", email);
        response.put("accessToken", token);

        return ResponseEntity.ok(response);
    }
}