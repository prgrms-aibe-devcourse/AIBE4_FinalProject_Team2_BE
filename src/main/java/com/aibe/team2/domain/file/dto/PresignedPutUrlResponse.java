package com.aibe.team2.domain.file.dto;

public record PresignedPutUrlResponse(
        String url,
        String key,
        int expiresInMinutes
) {}