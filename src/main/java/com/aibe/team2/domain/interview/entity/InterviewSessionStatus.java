package com.aibe.team2.domain.interview.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InterviewSessionStatus {
    CREATED("세션 생성됨"),
    IN_PROGRESS("면접 진행 중"),
    DONE("면접 정상 종료"),
    ABORTED("면접 중단됨");

    private final String description;
}