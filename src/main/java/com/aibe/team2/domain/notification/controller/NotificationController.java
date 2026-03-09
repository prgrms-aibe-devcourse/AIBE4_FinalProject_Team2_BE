package com.aibe.team2.domain.notification.controller;

import com.aibe.team2.domain.auth.dto.CustomUserDetails;
import com.aibe.team2.domain.notification.service.NotificationService;
import com.aibe.team2.global.common.annotation.LoginMemberId;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // 1. SSE 구독 - 클라이언트가 최초 로그인 시 실시간 알림 파이프 연결
    // GET http://localhost:8081/api/v1/notifications/subscribe/1
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(
            @LoginMemberId Long memberId
    ) {
        return notificationService.subscribe(memberId);
    }
    // 2. 알림 조회 - 회원의 전체 알림 목록을 최신순으로 조회(종 모양 클릭 시 드롭다운 표시용)
    // GET http://localhost:8081/api/v1/notifications?memberId=1
    @GetMapping
    public ResponseEntity<?> getNotifications (@LoginMemberId Long memberId) {
        return ResponseEntity.ok(notificationService.getNotifications(memberId));
    }

    // 3. 알림 관리(읽음 처리) - 특정 알림을 클릭했을 때 "안읽음" 상태를 "읽음" 상태로 변경
    // PATCH http://localhost:8081/api/v1/notifications/1?memberId=1
    @PatchMapping("/{notificationId}")
    public ResponseEntity<?> markAsRead(
            @PathVariable("notificationId") Long notificationId,
            @LoginMemberId Long memberId
    ) {
        notificationService.markAsRead(notificationId, memberId);
        return ResponseEntity.ok("알림이 읽음 처리되었습니다.");
    }

    // 4. 알림 관리(삭제) - 특정 알림을 목록에서 완전히 삭제
    // DELETE http://localhost:8081/api/v1/notifications/1
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<?> deleteNotification(
            @PathVariable("notificationId") Long notificationId,
            @LoginMemberId Long memberId
    ) {
        notificationService.deleteNotification(notificationId, memberId);
        return ResponseEntity.ok("알림이 삭제되었습니다.");
    }

    // 5. 안읽은 알림 개수 조회 API - 알림 아이콘에 표시할 숫자 반환
    // GET http://localhost:8081/api/v1/notifications/unread-count?memberId=1
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(
            @LoginMemberId Long memberId
    ) {
        long unreadCount = notificationService.getUnreadNotificationCount(memberId);
        return ResponseEntity.ok(unreadCount);
    }

    // 6. [테스트용] 강제 알림 생성 및 전송 API
    // POST http://localhost:8081/api/v1/notifications/test-send?memberId=1
    @Profile("!prod")
    @PostMapping("/test-send")
    public ResponseEntity<String> testSend(
            @LoginMemberId Long memberId
    ) {
        String testMessage = "축하합니다! AI 자기소개서 분석이 완료되었습니다. (테스트)";
        String notificationType = "AI_ANALYSIS";

        notificationService.send(memberId, notificationType, testMessage);

        return ResponseEntity.ok("테스트 알림 발송 성공!");
    }
}
