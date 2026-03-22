package com.aibe.team2.domain.error.service;

import com.aibe.team2.domain.error.dto.admin.*;
import com.aibe.team2.domain.error.entity.ErrorIssue;
import com.aibe.team2.domain.error.entity.ErrorLog;
import com.aibe.team2.domain.error.repository.ErrorIssueRepository;
import com.aibe.team2.domain.error.repository.ErrorLogRepository;
import com.aibe.team2.domain.mypage.entity.Member;
import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.custom.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ErrorAdminService {

    private final ErrorIssueRepository errorIssueRepository;
    private final ErrorLogRepository errorLogRepository;

    private String buildStatusHint(LocalDateTime lastOccurredAt) {
        if (lastOccurredAt == null) {
            return "발생 이력 없음";
        }

        LocalDateTime now = LocalDateTime.now();

        if (lastOccurredAt.isAfter(now.minusHours(24))) {
            return "최근 24시간 내 재발";
        }

        if (lastOccurredAt.isAfter(now.minusDays(3))) {
            return "최근 3일 내 발생";
        }

        return "최근 3일 미발생";
    }

    private boolean isRecentlyOccurred(LocalDateTime lastOccurredAt) {
        return lastOccurredAt != null && lastOccurredAt.isAfter(LocalDateTime.now().minusHours(24));
    }

    public Page<ErrorIssueResponse> searchIssues(ErrorIssueSearchCond cond, Pageable pageable) {
        return errorIssueRepository.search(cond, pageable)
                .map(issue -> new ErrorIssueResponse(
                        issue,
                        buildStatusHint(issue.getLastOccurredAt()),
                        isRecentlyOccurred(issue.getLastOccurredAt())
                ));
    }

    public ErrorIssueDetailResponse getIssueDetail(Long issueId) {
        ErrorIssue issue = errorIssueRepository.findById(issueId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ERROR_ISSUE_NOT_FOUND));

        ErrorLog latestLog = errorLogRepository.findTopByIssueIdOrderByOccurredAtDesc(issueId)
                .orElse(null);

        String targetType = null;
        Long targetId = null;
        Long memberId = null;
        String memberEmail = null;
        String memberNickname = null;

        if (latestLog != null) {
            targetType = latestLog.getTargetType();
            targetId = latestLog.getTargetId();

            Member member = latestLog.getMember();
            if (member != null) {
                memberId = member.getMemberId();
                memberEmail = member.getEmail();
                memberNickname = member.getNickname();
            }
        }

        return new ErrorIssueDetailResponse(
                issue,
                targetType,
                targetId,
                memberId,
                memberEmail,
                memberNickname
        );
    }

    public Page<ErrorLogRowResponse> getLogsByIssue(Long issueId, Pageable pageable) {
        return errorLogRepository.findByIssueId(issueId, pageable)
                .map(ErrorLogRowResponse::new);
    }

    public ErrorLogDetailResponse getLogDetail(Long logId) {
        return errorLogRepository.findById(logId)
                .map(ErrorLogDetailResponse::new)
                .orElseThrow(() -> new NotFoundException(ErrorCode.ERROR_ISSUE_NOT_FOUND));
    }
}