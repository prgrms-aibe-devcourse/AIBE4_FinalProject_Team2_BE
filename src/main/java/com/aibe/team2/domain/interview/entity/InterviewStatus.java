package com.aibe.team2.domain.interview.entity;

public enum InterviewStatus {
    CREATED,      // 세션 생성됨
    READY,        // 질문 준비 완료
    ANSWERING,    // 답변 중
    COMPLETED     // 면접 종료
}
