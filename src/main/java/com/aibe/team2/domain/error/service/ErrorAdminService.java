package com.aibe.team2.domain.error.service;

import com.aibe.team2.domain.error.dto.admin.ErrorIssueDetailResponse;
import com.aibe.team2.domain.error.dto.admin.ErrorIssueResponse;
import com.aibe.team2.domain.error.dto.admin.ErrorIssueSearchCond;
import com.aibe.team2.domain.error.dto.admin.ErrorLogDetailResponse;
import com.aibe.team2.domain.error.dto.admin.ErrorLogRowResponse;
import com.aibe.team2.domain.error.entity.ErrorIssue;
import com.aibe.team2.domain.error.entity.ErrorLog;
import com.aibe.team2.domain.error.enums.IssueStatus;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ErrorAdminService {

    private final ErrorIssueRepository errorIssueRepository;
    private final ErrorLogRepository errorLogRepository;

    public Page<ErrorIssueResponse> searchIssues(ErrorIssueSearchCond cond, Pageable pageable) {
        return errorIssueRepository.search(cond, pageable)
                .map(ErrorIssueResponse::new);
    }

    public ErrorIssueDetailResponse getIssueDetail(Long issueId) {
        ErrorIssue issue = errorIssueRepository.findById(issueId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COMMON_404));

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
                .orElseThrow(() -> new NotFoundException(ErrorCode.COMMON_404));
    }

    @Transactional
    public IssueStatus updateIssueStatus(Long issueId, IssueStatus status) {
        ErrorIssue issue = errorIssueRepository.findById(issueId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.COMMON_404));

        issue.updateStatus(status);
        return issue.getStatus();
    }
}