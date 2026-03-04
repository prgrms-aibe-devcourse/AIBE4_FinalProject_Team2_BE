package com.aibe.team2.domain.interview.enums;

import java.util.Arrays;

public enum InterviewMode {
    NORMAL,    // 일반적인 기술/인성 면접
    FOLLOW_UP, // 지원자의 답변을 심층 분석하는 꼬리 질문 위주 면접
    STRESS;     // 날카로운 질문과 압박 수위가 높은 면접

    public static InterviewMode fromString(String mode) {
        if (mode == null || mode.isBlank()) {
            return NORMAL; // 기본값 설정 혹은 예외 처리
        }

        return Arrays.stream(InterviewMode.values())
                .filter(m -> m.name().equalsIgnoreCase(mode.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 면접 모드입니다: " + mode));
    }
}
