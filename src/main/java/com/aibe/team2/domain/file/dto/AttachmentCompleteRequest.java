package com.aibe.team2.domain.file.dto;

import com.aibe.team2.domain.file.entity.AttachmentFileType;
import com.aibe.team2.domain.file.entity.TargetType;

public record AttachmentCompleteRequest(
        String key,
        AttachmentFileType fileType,
        TargetType targetType,
        Long targetId
) {}