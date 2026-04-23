package com.aibe.team2.domain.interview.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JobRole {
    BACKEND("지원자의 희망 직무는 '백엔드 엔지니어'입니다. 서버, 데이터베이스, API 설계, 트랜잭션, 성능 최적화 관련 기술 질문을 우선적으로 던지세요."),
    FRONTEND("지원자의 희망 직무는 '프론트엔드 엔지니어'입니다. 렌더링 최적화, 상태 관리, 브라우저 동작 원리, UI/UX 구현 관련 기술 질문을 우선적으로 던지세요."),
    FULLSTACK("지원자의 희망 직무는 '풀스택 엔지니어'입니다. 프론트엔드부터 백엔드, DB까지 아우르는 전체적인 시스템 이해도와 통신 과정에 대한 질문을 던지세요."),
    PM("지원자의 희망 직무는 '서비스 기획자(PM/PO)'입니다. 데이터 기반 의사결정, 일정 및 리소스 관리, 개발자와의 협업 및 갈등 해결 경험에 대한 질문을 던지세요."),
    UNKNOWN("지원자의 희망 직무에 특화된 기술적 질문을 우선적으로 던지세요.");

    private final String instruction;

    // 리뷰 반영: Enum.valueOf() 대신 예외를 방지하는 커스텀 팩토리 메서드
    public static JobRole from(String value) {
        if (value == null || value.isBlank()) {
            return UNKNOWN;
        }
        for (JobRole role : values()) {
            if (role.name().equalsIgnoreCase(value.trim())) {
                return role;
            }
        }
        return UNKNOWN; // 매칭되지 않으면 에러(IllegalArgumentException) 대신 기본값 반환
    }
}