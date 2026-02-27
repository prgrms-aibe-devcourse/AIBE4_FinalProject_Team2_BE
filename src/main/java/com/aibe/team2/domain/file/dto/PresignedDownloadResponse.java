package com.aibe.team2.domain.file.dto;

public record PresignedDownloadResponse(
        String url,
        String key
) {}