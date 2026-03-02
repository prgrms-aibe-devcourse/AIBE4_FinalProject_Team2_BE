package com.aibe.team2.domain.file.dto;

import com.aibe.team2.domain.file.entity.AttachmentFileType;

public record AttachmentCompleteRequest(
        String key,
        AttachmentFileType fileType,
        String targetType,
        Long targetId
) {}