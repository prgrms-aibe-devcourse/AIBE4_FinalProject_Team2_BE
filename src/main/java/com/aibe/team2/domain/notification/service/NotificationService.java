package com.aibe.team2.domain.notification.service;

import com.aibe.team2.domain.mypage.entity.Member;
import com.aibe.team2.domain.mypage.repository.member.MemberRepository;
import com.aibe.team2.domain.notification.dto.NotificationResponse;
import com.aibe.team2.domain.notification.entity.Notification;
import com.aibe.team2.domain.notification.repository.NotificationRepository;
import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.custom.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;

    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;

    // 1. 구독 - 클라이언트가 처음 로그인했을 때 실시간 통신 파이프를 연결하는 메서드
    public SseEmitter subscribe(Long memberId) {
        String emitterId = memberId + "_" + System.currentTimeMillis();
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

        emitters.put(emitterId, emitter);

        // 네트워크 끊김이나 타임아웃 발생 시 메모리 누수를 막기 위해 객체 삭제
        emitter.onCompletion(() -> emitters.remove(emitterId));
        emitter.onTimeout(() -> emitters.remove(emitterId));
        emitter.onError((e) -> emitters.remove(emitterId));

        // 503 Service Unavailable 에러 방지를 위한 최초 더미 데이터 전송
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .id(emitterId)
                    .data("EventStream Created. [userId=" + memberId + "]"));

            // 2. 이 위치에 로그 출력 코드를 추가합니다.
            log.info("SSE 연결 완료 및 더미 데이터 전송 성공! memberId: {}", memberId);

        } catch (IOException e) {
            emitters.remove(emitterId);
            log.error("SSE 연결 오류 발생 memberId: {}", memberId, e);
        }

        return emitter;
    }

    // 2. 알림 저장 및 전송 - AI 첨삭 완료 시 호출되어 DB에 알림을 저장하고 프론트엔드로 즉시 전송
    @Async
    @Transactional
    public void send(Long memberId, String notificationType, String message) {

        // a. 영속성 컨텍스트에서 관리되는 엔티티로 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND));

        // b. 조회된 member 객체를 사용하여 알림 생성
        Notification notification = Notification.builder()
                .member(member)
                .notificationType(notificationType)
                .message(message)
                .build();

        notificationRepository.save(notification);
        sendToClient(memberId, message);
    }

    // 3. 클라이언트 전송 내부 로직
    private void sendToClient(Long memberId, Object data) {
        Map<String, SseEmitter> userEmitters = emitters.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(memberId + "_"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        userEmitters.forEach(
                (key, emitter) -> {
                    try {
                        emitter.send(SseEmitter.event()
                                .name("sse")
                                .id(key)
                                .data(data)
                        );
                    } catch (IOException exception) {
                        emitters.remove(key);
                        log.error("SSE 전송 실패, Emitter 삭제: {}", key, exception);
                    }
                }
        );
    }

    // 4. 알림 목록 조회
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications(Long memberId) {
        List<Notification> notifications = notificationRepository.findByMember_MemberIdOrderByCreatedAtDesc(memberId);
        return notifications.stream()
                .map(NotificationResponse::new)
                .toList();
    }

    // 5. 알림 읽음 처리
    @Transactional
    public void markAsRead(Long notificationId, Long requesterId) {
        // a. 알림 조회
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOTIFICATION_NOT_FOUND));

        // b. 비즈니스 룰 1번 적용: 권한 체크 (소유자 ID vs 요청자 ID)
        if (!notification.getMember().getMemberId().equals(requesterId)) {
            // 현업에서는 403 에러를 던지는 커스텀 예외를 사용합니다.
            throw new AccessDeniedException(ErrorCode.COMMON_403.getMessage());
        }

        // c. 비즈니스 룰 2, 3번 적용: 읽음 상태로 업데이트 (이미 true여도 true로 덮어쓰므로 멱등성 보장)
        notification.markAsRead();
    }

    // 6. 알림 삭제 처리
    @Transactional
    public void deleteNotification(Long notificationId, Long requesterId) { // 1. 매개변수 추가
        // 1. 지우려는 알림이 DB에 존재하는지 먼저 조회합니다.
        // 만약 이미 지워졌거나 없는 ID라면 우리가 만든 NotFoundException 에러를 터뜨립니다.
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOTIFICATION_NOT_FOUND));

        // 2. 비즈니스 룰 적용 : 권한 체크(소유자 ID와 삭제 요청자 ID 일치)
        if (!notification.getMember().getMemberId().equals(requesterId)) { // 2. 중괄호 시작
            log.warn("알림 삭제 권한 없음. notificationId: {}, requesterId: {}", notificationId, requesterId);
            throw new AccessDeniedException(ErrorCode.COMMON_403.getMessage());
        } // 2. 중괄호 끝

        // 3. 알림이 존재한다는 것이 확인되면 삭제를 진행합니다.
        notificationRepository.delete(notification);
    }

    // 7. 안읽은 알림 개수 조회
    @Transactional(readOnly = true)
    public long getUnreadNotificationCount(Long memberId) {
        return notificationRepository.countByMember_MemberIdAndIsReadFalse(memberId);
    }
}