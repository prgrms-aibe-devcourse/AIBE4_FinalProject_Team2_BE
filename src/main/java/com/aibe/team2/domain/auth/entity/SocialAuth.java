package com.aibe.team2.domain.auth.entity;

import com.aibe.team2.domain.mypage.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "social_auth")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class) // 날짜 자동 생성을 위해 필요
public class SocialAuth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 회원 ID (Member 엔티티와의 연관관계 매핑)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 제공자 측 유저 ID
    @Column(name = "provider_member_id", nullable = false)
    private String providerMemberId;

    // 제공자 타입 (GOOGLE, KAKAO, GITHUB)
    @Column(name = "provider_type", length = 50, nullable = false)
    private String providerType;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Soft Delete를 위한 필드
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * Soft Delete 처리 메서드
     */
    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}