package com.aibe.team2.domain.interview.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExperienceLevel {
    NEWBIE("지원자는 '신입(경력 없음)'입니다. 너무 깊은 아키텍처 질문은 피하고, 기초적인 전공 지식, 열정, 문제 해결을 위한 접근 방식, 그리고 잠재력을 위주로 평가하세요."),
    JUNIOR("지원자는 '주니어(1~3년 차)'입니다. 실무에서의 구체적인 트러블슈팅 경험, 협업 태도, 그리고 기본 기술 스택 활용 능력을 검증하세요."),
    MIDDLE("지원자는 '미들(4~7년 차)'입니다. 시스템 아키텍처 설계, 대규모 데이터 성능 최적화 경험 등 심도 있는 기술 역량을 강하게 검증하세요."),
    SENIOR("지원자는 '시니어(8년 차 이상)'입니다. 기술 스택 결정 권한, 프로젝트 리딩 경험, 아키텍처 설계 의도 및 비즈니스 임팩트에 대한 날카로운 질문을 던지세요."),
    UNKNOWN("지원자의 경력에 맞는 적절한 질문을 던지세요.");

    private final String instruction;

    // 리뷰 반영: 안전한 파싱 메서드
    public static ExperienceLevel from(String value) {
        if (value == null || value.isBlank()) {
            return UNKNOWN;
        }
        for (ExperienceLevel level : values()) {
            if (level.name().equalsIgnoreCase(value.trim())) {
                return level;
            }
        }
        return UNKNOWN;
    }
}