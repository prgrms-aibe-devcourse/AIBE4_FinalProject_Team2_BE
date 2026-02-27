package com.aibe.team2.domain.file.service;

import com.aibe.team2.domain.file.dto.AttachmentCompleteRequest;
import com.aibe.team2.domain.file.dto.AttachmentCompleteResponse;
import com.aibe.team2.domain.file.dto.PresignedDownloadResponse;
import com.aibe.team2.domain.file.entity.Attachment;
import com.aibe.team2.domain.file.repository.AttachmentRepository;
import com.aibe.team2.global.error.ErrorCode;
import com.aibe.team2.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final S3PresignedService s3PresignedService;

    @Transactional
    public AttachmentCompleteResponse complete(AttachmentCompleteRequest req, Long ownerMemberId) {

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

        // TODO: validateDownloadAllowed(att.getTargetType(), att.getTargetId());

        var presigned = s3PresignedService.generateGetPresignedUrl(att.getS3Key());
        return new PresignedDownloadResponse(presigned.url(), presigned.key());
    }
}