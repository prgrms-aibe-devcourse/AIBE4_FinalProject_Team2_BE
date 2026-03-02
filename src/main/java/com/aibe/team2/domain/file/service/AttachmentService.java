package com.aibe.team2.domain.file.service;

import com.aibe.team2.domain.file.dto.AttachmentCompleteRequest;
import com.aibe.team2.domain.file.dto.AttachmentCompleteResponse;
import com.aibe.team2.domain.file.dto.PresignedDownloadResponse;
import com.aibe.team2.domain.file.entity.Attachment;
import com.aibe.team2.domain.file.entity.TargetType;
import com.aibe.team2.domain.file.repository.AttachmentRepository;
import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.aibe.team2.domain.resume.repository.ResumeRepository;
import com.aibe.team2.domain.resume.repository.ResumeAnalysisRepository;
import com.aibe.team2.domain.statistics.repository.interview.InterviewRecordRepository;
import com.aibe.team2.domain.interview.repository.InterviewSessionRepository;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final S3PresignedService s3PresignedService;

    private final ResumeRepository resumeRepository;
    private final ResumeAnalysisRepository resumeAnalysisRepository; // analysis_report
    private final InterviewRecordRepository interviewRecordRepository;
    private final InterviewSessionRepository interviewSessionRepository;

    @Transactional
    public AttachmentCompleteResponse complete(AttachmentCompleteRequest req, Long ownerMemberId) {

        // 0) targetType/targetId 소유권 검증
        validateTargetOwnership(req.targetType(), req.targetId(), ownerMemberId);

        // 1) S3에 실제로 업로드 되었는지 검증 (없으면 FILE_NOT_FOUND)
        s3PresignedService.headObject(req.key());

        // 2) DB 저장
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
                .orElseThrow(() -> new BusinessException(ErrorCode.FILE_NOT_FOUND));

        // 소유자 또는 관리자만
        if (!isAdmin && !att.getOwnerMemberId().equals(requesterId)) {
            throw new BusinessException(ErrorCode.COMMON_403);
        }

        var presigned = s3PresignedService.generateGetPresignedUrl(att.getS3Key());
        return new PresignedDownloadResponse(presigned.url(), presigned.key());
    }

    private void validateTargetOwnership(String rawTargetType, Long targetId, Long ownerMemberId) {
        if (rawTargetType == null || rawTargetType.isBlank() || targetId == null || targetId <= 0) {
            throw new BusinessException(ErrorCode.COMMON_400);
        }

        final TargetType targetType;
        try {
            targetType = TargetType.valueOf(rawTargetType);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.COMMON_400);
        }

        switch (targetType) {
            case RESUME -> {
                var resume = resumeRepository.findById(targetId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_404));

                // TODO: 아래 getter는 너희 Resume 엔티티에 맞춰 (getMemberId() 혹은 getMember().getId())
                Long resumeOwnerId = resume.getMemberId();

                if (!ownerMemberId.equals(resumeOwnerId)) {
                    throw new BusinessException(ErrorCode.COMMON_403);
                }
            }

            case INTERVIEW_RECORD -> {
                var record = interviewRecordRepository.findById(targetId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_404));

                // TODO: 아래 getter는 엔티티 구조에 맞춰 조정
                Long sessionId = record.getInterviewSession().getId();

                var session = interviewSessionRepository.findById(sessionId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_404));

                Long sessionOwnerId = session.getMemberId();

                if (!ownerMemberId.equals(sessionOwnerId)) {
                    throw new BusinessException(ErrorCode.COMMON_403);
                }
            }

            case ANALYSIS_REPORT -> {
                var report = resumeAnalysisRepository.findById(targetId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_404));

                Long resumeId = report.getResume().getId();

                var resume = resumeRepository.findById(resumeId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.COMMON_404));

                Long resumeOwnerId = resume.getMemberId();

                if (!ownerMemberId.equals(resumeOwnerId)) {
                    throw new BusinessException(ErrorCode.COMMON_403);
                }
            }

            default -> throw new BusinessException(ErrorCode.COMMON_400);
        }
    }
}