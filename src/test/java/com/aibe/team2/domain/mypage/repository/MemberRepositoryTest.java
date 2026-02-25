package com.aibe.team2.domain.mypage.repository;

import com.aibe.team2.domain.mypage.entity.Member;
import com.aibe.team2.domain.mypage.entity.enums.Provider;
import com.aibe.team2.domain.mypage.entity.enums.Role;
import com.aibe.team2.global.config.QuerydslConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

// 🎯 검증을 위한 AssertJ 라이브러리의 올바른 임포트 경로입니다.
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QuerydslConfig.class)
class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("QueryDSL을 이용한 프로필 조회 테스트가 정상 작동한다.")
    void findProfileByIdWithQueryDSL_Test() {
        // Given: 더미 멤버 데이터 저장 (기존에 작성해두신 생성자 활용)
        Member newMember = new Member(
                "test@example.com", // email
                "password123",      // password
                "개발왕",            // nickname
                Role.MEMBER,        // role
                Provider.LOCAL      // provider (원하시는 Provider Enum으로 변경 가능)
        );
        Member savedMember = memberRepository.save(newMember);

        // When: 작성한 QueryDSL 메서드 호출
        Optional<Member> foundMember = memberRepository.findProfileByIdWithQueryDSL(savedMember.getMemberId());

        // Then: 저장한 데이터와 QueryDSL로 찾은 데이터가 일치하는지 확인
        assertThat(foundMember).isPresent(); // 데이터가 존재하는지 확인
        assertThat(foundMember.get().getNickname()).isEqualTo("개발왕"); // 닉네임 일치 확인
    }
}