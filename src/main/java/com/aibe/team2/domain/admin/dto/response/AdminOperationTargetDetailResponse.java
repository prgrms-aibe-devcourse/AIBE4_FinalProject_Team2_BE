package com.aibe.team2.domain.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AdminOperationTargetDetailResponse {
    private String targetType;
    private Long targetId;

    private String currentStatus;
    private String latestQueueStatus;
    private Integer retryCount;
    private String latestErrorMessage;
    private LocalDateTime lastProcessedAt;

    private Long memberId;
    private String memberEmail;
    private String memberNickname;

    private boolean retryable;
    private boolean cancellable;
}