package com.aibe.team2.domain.auth.repository;

import com.aibe.team2.domain.auth.entity.SocialAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SocialAuthRepository extends JpaRepository<SocialAuth, Long> {
    // 특정 소셜 서비스의 고유 ID와 타입으로 연동 정보 찾기
    Optional<SocialAuth> findByProviderMemberIdAndProviderType(String providerMemberId, String providerType);

    // Repository 수정 예시
    @Query("SELECT s FROM SocialAuth s JOIN FETCH s.member WHERE s.providerMemberId = :id AND s.providerType = :type AND s.deletedAt IS NULL")
    Optional<SocialAuth> findActiveSocialAuth(@Param("id") String id, @Param("type") String type);
}