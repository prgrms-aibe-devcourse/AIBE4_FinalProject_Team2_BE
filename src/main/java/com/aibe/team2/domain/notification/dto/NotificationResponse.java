package com.aibe.team2.domain.notification.dto;

import com.aibe.team2.domain.notification.entity.Notification;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class NotificationResponse {
    private Long id;
    private String message;
    private String notificationType;
    private boolean isRead;
    private LocalDateTime createdAt;

    // 엔티티를 DTO로 변환하는 생성자
    public NotificationResponse(Notification notification) {
        this.id = notification.getId();
        this.message = notification.getMessage();
        this.notificationType = notification.getNotificationType();
        this.isRead = notification.isRead();
        this.createdAt = notification.getCreatedAt();
    }
}