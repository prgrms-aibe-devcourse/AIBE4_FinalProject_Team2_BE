package com.aibe.team2.domain.notification.repository;

import com.aibe.team2.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    long countByMember_MemberIdAndIsReadFalse(Long memberId);

    List<Notification> findByMember_MemberIdOrderByCreatedAtDesc(Long memberId);
}