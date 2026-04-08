package com.aibe.team2.domain.auth.controller;

import com.aibe.team2.domain.auth.service.FindService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/find")
@RequiredArgsConstructor
public class FindController {

    private final FindService findService;

    // 이메일 찾기
    @GetMapping("/email")
    public ResponseEntity<String> findEmail(@RequestParam String nickname) {
        return ResponseEntity.ok(findService.findEmail(nickname));
    }

    // 비밀번호 찾기 (임시 비밀번호 발급)
    @PostMapping("/password")
    public ResponseEntity<String> findPassword(@RequestBody Map<String, String> request) {
        findService.sendTemporaryPassword(request.get("email"));
        return ResponseEntity.ok("입력하신 이메일로 임시 비밀번호를 전송했습니다.");
    }
}