package com.aibe.team2.domain.file.dto;

public record PresignedPutUrlRequest(String fileName, String contentType) {}