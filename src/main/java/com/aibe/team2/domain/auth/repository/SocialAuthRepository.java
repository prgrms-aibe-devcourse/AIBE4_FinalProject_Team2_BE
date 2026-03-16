package com.aibe.team2.domain.auth.repository;

import com.aibe.team2.domain.auth.entity.SocialAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SocialAuthRepository extends JpaRepository<SocialAuth, Long> {

    @Query("SELECT s FROM SocialAuth s JOIN FETCH s.member " +
            "WHERE s.providerMemberId = :providerMemberId AND s.providerType = :providerType")
    Optional<SocialAuth> findByProviderMemberIdAndProviderTypeWithMember(
            @Param("providerMemberId") String providerMemberId,
            @Param("providerType") String providerType);
}