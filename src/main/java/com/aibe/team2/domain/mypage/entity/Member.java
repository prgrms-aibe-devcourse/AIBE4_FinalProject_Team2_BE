package com.aibe.team2.domain.mypage.entity;

import com.aibe.team2.domain.mypage.entity.enums.MemberStatus;
import com.aibe.team2.domain.mypage.entity.enums.Provider;
import com.aibe.team2.domain.mypage.entity.enums.Role;
import com.aibe.team2.domain.mypage.entity.enums.SubscriptionPlan;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long memberId;

    @Column(nullable = false, unique = true)
    private String email;

    private String password;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider")
    private Provider provider;

    @Column(name = "desired_job", length = 100)
    private String desiredJobRole;

    @Column(name = "preferred_location", length = 100)
    private String preferredLocation;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_plan")
    private SubscriptionPlan subscriptionPlan;

    @Column(name = "credit_balance")
    private Integer creditBalance;

    @CreatedDate
    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MemberStatus status;

    public Member(
            String email, String password, String nickname, Role role, Provider provider
    ) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = role != null ? role : Role.MEMBER;
        this.provider = provider;
        this.subscriptionPlan = SubscriptionPlan.FREE;
        this.creditBalance = 0;
        this.status = MemberStatus.ACTIVE;
    }

    public void updateProfile(String nickname, String profileImageUrl) {
        if (nickname != null && !nickname.isEmpty()) this.nickname = nickname;
        if (profileImageUrl != null && !profileImageUrl.isEmpty()) this.profileImageUrl = profileImageUrl;
    }

    public void updateProfileImage(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void updatePassword(String encodedNewPassword) {
        this.password = encodedNewPassword;
    }

    public void updateJobPreferences(String desiredJobRole, String preferredLocation) {
        this.desiredJobRole = desiredJobRole;
        this.preferredLocation = preferredLocation;
    }

    public void updateCreditBalance(int newBalance) {
        this.creditBalance = newBalance;
    }

    public void updateStatus(MemberStatus status) {
        this.status = status;

        if (status == MemberStatus.DELETED) {
            this.deletedAt = LocalDateTime.now();
        } else {
            this.deletedAt = null; // 복구 시 삭제 시간 제거
        }
    }
}