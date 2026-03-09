package com.aibe.team2.domain.file.service;

import com.aibe.team2.domain.file.dto.AttachmentCompleteRequest;
import com.aibe.team2.domain.file.dto.AttachmentCompleteResponse;
import com.aibe.team2.domain.file.dto.PresignedDownloadResponse;
import com.aibe.team2.domain.file.entity.Attachment;
import com.aibe.team2.domain.file.entity.TargetType;
import com.aibe.team2.domain.file.repository.AttachmentRepository;
import com.aibe.team2.domain.resume.entity.AnalysisStatus;
import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.BusinessException;
import com.aibe.team2.global.exception.custom.FileException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aibe.team2.domain.resume.repository.ResumeRepository;
import com.aibe.team2.domain.resume.repository.ResumeAnalysisRepository;
import com.aibe.team2.domain.statistics.repository.interview.InterviewRecordRepository;

import com.aibe.team2.domain.resume.entity.AnalyzedReport;
import com.aibe.team2.domain.statistics.entity.InterviewRecord;
import com.aibe.team2.domain.interview.enums.InterviewSessionStatus;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final S3PresignedService s3PresignedService;

    private final ResumeRepository resumeRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository;
    private final InterviewRecordRepository interviewRecordRepository;

    @Transactional
    public AttachmentCompleteResponse complete(AttachmentCompleteRequest req, Long ownerMemberId) {

        // 0) targetType/targetId 소유권 검증
        validateTargetOwnership(req.targetType(), req.targetId(), ownerMemberId);

        // 1) key가 내 prefix인지 검증 (IDOR 최소 방어)
        validateKeyOwnership(req.key(), ownerMemberId);

        // 2) S3 업로드 여부 + 실제 용량 검증
        var head = s3PresignedService.headObject(req.key());
        s3PresignedService.validateUploadedSize(req.fileType(), head.contentLength());

        // 3) DB 저장
        Attachment saved = attachmentRepository.save(
                Attachment.builder()
                        .ownerMemberId(ownerMemberId)
                        .s3Key(req.key())
                        .fileType(req.fileType())
                        .targetType(req.targetType())
                        .targetId(req.targetId())
                        .build()
        );

        return new AttachmentCompleteResponse(saved.getId(), saved.getS3Key());
    }

    @Transactional(readOnly = true)
    public PresignedDownloadResponse presignDownload(Long attachmentId, Long requesterId, boolean isAdmin) {

        Attachment att = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new FileException(ErrorCode.FILE_NOT_FOUND));

        // 소유자 또는 관리자만
        if (!isAdmin && !att.getOwnerMemberId().equals(requesterId)) {
            throw new BusinessException(ErrorCode.COMMON_403);
        }

        // COMPLETED(또는 DONE) 상태만 다운로드 허용
        validateDownloadAllowed(att);

        var presigned = s3PresignedService.generateGetPresignedUrl(att.getS3Key());
        return new PresignedDownloadResponse(presigned.url(), presigned.key());
    }

    private void validateKeyOwnership(String key, Long ownerMemberId) {
        if (key == null || key.isBlank() || ownerMemberId == null || ownerMemberId <= 0) {
            throw new BusinessException(ErrorCode.COMMON_400);
        }

        String prefix = "uploads/members/" + ownerMemberId + "/";
        if (!key.startsWith(prefix)) {
            throw new BusinessException(ErrorCode.COMMON_403);
        }
    }

    private void validateTargetOwnership(TargetType targetType, Long targetId, Long ownerMemberId) {
        if (targetType == null || targetId == null || targetId <= 0) {
            throw new BusinessException(ErrorCode.COMMON_400);
        }

        switch (targetType) {
            case RESUME -> {
                var resume = resumeRepository.findById(targetId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.RESUME_NOT_FOUND));

                Long resumeOwnerId = resume.getMemberId();
                if (!ownerMemberId.equals(resumeOwnerId)) {
                    throw new BusinessException(ErrorCode.COMMON_403);
                }
            }

            case INTERVIEW_RECORD -> {
                var record = interviewRecordRepository.findById(targetId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.INTERVIEW_RECORD_NOT_FOUND));

                Long sessionOwnerId = record.getInterviewSession().getMemberId();
                if (!ownerMemberId.equals(sessionOwnerId)) {
                    throw new BusinessException(ErrorCode.COMMON_403);
                }
            }

            case ANALYSIS_REPORT -> {
                var report = resumeAnalysisRepository.findById(targetId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.ANALYSIS_REPORT_NOT_FOUND));

                Long resumeOwnerId = report.getResume().getMemberId();
                if (!ownerMemberId.equals(resumeOwnerId)) {
                    throw new BusinessException(ErrorCode.COMMON_403);
                }
            }
        }
    }

    private void validateDownloadAllowed(Attachment att) {
        TargetType targetType = att.getTargetType();

        switch (targetType) {
            case RESUME -> {
                return;
            }
            case ANALYSIS_REPORT -> {
                AnalyzedReport report = resumeAnalysisRepository.findById(att.getTargetId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.ANALYSIS_REPORT_NOT_FOUND));

                if (report.getStatus() != AnalysisStatus.COMPLETED) {
                    throw new BusinessException(ErrorCode.ANALYSIS_IN_PROGRESS);
                }
            }
            case INTERVIEW_RECORD -> {
                InterviewRecord record = interviewRecordRepository.findById(att.getTargetId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.INTERVIEW_RECORD_NOT_FOUND));

                if (record.getInterviewSession().getStatus() != InterviewSessionStatus.DONE) {
                    throw new BusinessException(ErrorCode.INTERVIEW_NOT_COMPLETED);
                }
            }
        }
    }
}