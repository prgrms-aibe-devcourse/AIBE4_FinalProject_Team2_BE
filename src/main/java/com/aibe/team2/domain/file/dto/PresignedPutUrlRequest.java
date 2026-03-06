package com.aibe.team2.domain.file.dto;

import com.aibe.team2.domain.file.entity.AttachmentFileType;

public record PresignedPutUrlRequest(
        String fileName,
        String contentType,
        AttachmentFileType fileType,
        Long fileSize
) {}