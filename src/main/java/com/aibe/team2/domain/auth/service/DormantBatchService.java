package com.aibe.team2.domain.auth.service;

import com.aibe.team2.domain.mypage.entity.Member;
import com.aibe.team2.domain.mypage.entity.enums.MemberStatus;
import com.aibe.team2.domain.mypage.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DormantBatchService {

    private final MemberRepository memberRepository;
    private final EmailService emailService;

    // 매일 자정에 실행 (초 분 시 일 월 요일)
    @Scheduled(cron = "0 0 0 * * *")
    @Async
    @Transactional
    public void convertToDormantAccounts() {
        // 기준 시간: 현재로부터 1년 전 (프로젝트 요구사항에 따라 3개월, 6개월 등으로 변경 가능)
        LocalDateTime thresholdDate = LocalDateTime.now().minusYears(1);

        // 1. 마지막 로그인 날짜가 기준일 이전이고, 현재 ACTIVE인 회원 조회
        List<Member> targetMembers = memberRepository.findAllByStatusAndLastLoginAtBefore(
                MemberStatus.ACTIVE, thresholdDate);

        for (Member member : targetMembers) {
            // 1. 상태 변경
            member.updateStatus(MemberStatus.DORMANCY);

            // 2. 메일 발송
            String html = createDormantNotificationHtml(member.getNickname());
            emailService.sendHtmlMessage(member.getEmail(), "[SyncTalk] 장기 미접속 계정 휴면 전환 안내", html);
        }
    }

    public String createDormantNotificationHtml(String nickname) {
        return "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #e0e0e0; border-radius: 10px; overflow: hidden;'>" +
                // 헤더 섹션 (SyncTalk 테마 컬러 유지)
                "<div style='background-color: #1976D2; padding: 20px; text-align: center;'>" +
                "<h1 style='color: white; margin: 0; font-size: 24px;'>SyncTalk</h1>" +
                "</div>" +

                // 본문 섹션
                "<div style='padding: 40px 20px; text-align: center; color: #333;'>" +
                "<h2 style='margin-bottom: 20px;'>장기 미접속에 따른 휴면 전환 안내</h2>" +
                "<p style='font-size: 16px; color: #666; line-height: 1.6;'>" +
                "안녕하세요, <strong>" + nickname + "</strong>님.<br/>" +
                "SyncTalk을 오랫동안 이용하지 않아 고객님의 소중한 정보를 보호하기 위해<br/>" +
                "해당 계정을 <strong>휴면 상태</strong>로 전환하였습니다." +
                "</p>" +

                // 안내 박스
                "<div style='margin: 30px auto; padding: 25px; background-color: #f8f9fa; border-radius: 8px; border-left: 5px solid #1976D2; text-align: left;'>" +
                "<p style='margin: 0 0 10px 0; font-size: 14px;'><strong>전환 일시:</strong> " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")) + "</p>" +
                "<p style='margin: 0; font-size: 14px;'><strong>보관 항목:</strong> 가입 정보, 면접 리포트, 자기소개서 데이터 등</p>" +
                "</div>" +

                "<p style='font-size: 15px; color: #333; margin-top: 30px; font-weight: bold;'>" +
                "다시 돌아오시겠어요?" +
                "</p>" +
                "<p style='font-size: 14px; color: #666; margin-bottom: 25px;'>" +
                "SyncTalk 홈페이지에서 평소와 같이 로그인하시면<br/>즉시 휴면 상태가 해제되어 모든 서비스를 이용하실 수 있습니다." +
                "</p>" +

                // 메인 버튼
                "<a href='https://synctalk-url.com/login' style='display: inline-block; padding: 12px 30px; background-color: #1976D2; color: white; text-decoration: none; border-radius: 5px; font-weight: bold;'>서비스 바로가기</a>" +
                "</div>" +

                // 푸터 섹션
                "<div style='background-color: #f1f1f1; padding: 20px; text-align: center; font-size: 12px; color: #888;'>" +
                "본 메일은 관련 법령에 의거하여 발송되는 안내 메일입니다.<br/>" +
                "궁금하신 점은 고객센터를 이용해 주세요.<br/>" +
                "© 2026 SyncTalk Team 2. All rights reserved." +
                "</div>" +
                "</div>";
    }
}