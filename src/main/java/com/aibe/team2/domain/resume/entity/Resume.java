package com.aibe.team2.domain.resume.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 아래의 코드는 전부 예시입니다. 추후 기능이나 필요에 맞게 자유롭게 수정하면 됩니다.
 */
@Entity
@Table(name = "resume") // 컨벤션: snake_case 테이블명
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 컨벤션: 기본 생성자 지양
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @Builder // 컨벤션: @Setter 대신 빌더 사용
    public Resume(String title, String content, String feedback) {
        this.title = title;
        this.content = content;
        this.feedback = feedback;
    }

    // 컨벤션: 의미 있는 비즈니스 메서드 사용 (Why에 집중)
    public void updateFeedback(String feedback) {
        this.feedback = feedback;
    }
}