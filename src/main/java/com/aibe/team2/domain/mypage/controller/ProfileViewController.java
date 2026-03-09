package com.aibe.team2.domain.mypage.controller;

import com.aibe.team2.domain.mypage.dto.response.MemberResponse;
import com.aibe.team2.domain.mypage.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Slf4j
@RequiredArgsConstructor
public class ProfileViewController {

    private final MemberService memberService;

    // 프론트엔드 전환 전 임시 화면 확인용 URL
    @GetMapping("/view/profile")
    public String profileSettingsView(Model model) {
        // 1. 임시 ID 1L로 회원 정보(DTO) 조회
        MemberResponse profileData = memberService.getMemberInfo(1L);

        // [검증 코드]
        log.info("조회된 닉네임: " + profileData.getNickname());
        log.info("조회된 이메일: " + profileData.getEmail());

        // 2. 타임리프에서 사용할 데이터 바인딩
        model.addAttribute("profile", profileData);

        // 3. 렌더링할 HTML 파일명 (src/main/resources/templates/profile-settings.html)
        return "html/profile-settings";
    }
}