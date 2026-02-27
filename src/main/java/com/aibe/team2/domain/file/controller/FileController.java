package com.aibe.team2.domain.file.controller;

import com.aibe.team2.domain.file.service.S3PresignedService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final S3PresignedService s3PresignedService;

    public FileController(S3PresignedService s3PresignedService) {
        this.s3PresignedService = s3PresignedService;
    }

    @PostMapping("/presigned-url")
    public ResponseEntity<S3PresignedService.PresignedUrlResponse> createPresignedUrl(
            @RequestBody PresignedUrlRequest request
    ) {
        return ResponseEntity.ok(
                s3PresignedService.generatePutPresignedUrl(request.fileName(), request.contentType())
        );
    }

    public record PresignedUrlRequest(String fileName, String contentType) {}
}