package com.aibe.team2.domain.mypage.entity;

import com.aibe.team2.domain.mypage.entity.enums.Role;
import com.aibe.team2.domain.mypage.entity.enums.Provider;
import com.aibe.team2.domain.mypage.entity.enums.SubscriptionPlan;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public Member(
            String email, String password, String nickname, Role role, Provider provider
    ){
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = role != null ? role : Role.MEMBER;
        this.provider = provider;

        this.subscriptionPlan = SubscriptionPlan.FREE;
        this.creditBalance = 0;

        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateProfile(String nickname, String profileImageUrl) {
        if(nickname != null && !nickname.isEmpty())
            this.nickname = nickname;
        if(profileImageUrl != null && !profileImageUrl.isEmpty())
            this.profileImageUrl = profileImageUrl;
        this.updatedAt = LocalDateTime.now();
    }

    public void updatePassword(String encodedNewPassword) {
        this.password = encodedNewPassword;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateJobPreferences(String desiredJobRole, String preferredLocation) {
        this.desiredJobRole = desiredJobRole;
        this.preferredLocation = preferredLocation;
        this.updatedAt = LocalDateTime.now();
    }
}
