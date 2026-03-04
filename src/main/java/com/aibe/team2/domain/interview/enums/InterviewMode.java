package com.aibe.team2.domain.interview.enums;

import java.util.Arrays;

public enum InterviewMode {
    NORMAL,    // 일반적인 기술/인성 면접
    FOLLOW_UP, // 지원자의 답변을 심층 분석하는 꼬리 질문 위주 면접
    STRESS;    // 날카로운 질문과 압박 수위가 높은 면접

    /**
     * 안전한 Enum 파싱을 위한 정적 팩토리 메서드
     * valueOf()의 500 에러를 방지, 대소문자 무시 및 예외 메시지 커스텀이 가능
     */
    public static InterviewMode from(String value) {
        if (value == null || value.isBlank()) {
            return NORMAL; // 기본값 반환
        }

        return Arrays.stream(InterviewMode.values())
                .filter(mode -> mode.name().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 면접 모드입니다: " + value));
    }
}