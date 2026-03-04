package com.aibe.team2.domain.interview.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InterviewSessionStatus {
    CREATED, IN_PROGRESS, DONE, ABORTED
}