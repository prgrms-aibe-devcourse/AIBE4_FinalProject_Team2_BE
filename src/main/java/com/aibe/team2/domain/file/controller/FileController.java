package com.aibe.team2.domain.file.controller;

import com.aibe.team2.domain.file.dto.*;
import com.aibe.team2.domain.file.service.AttachmentService;
import com.aibe.team2.domain.file.service.S3PresignedService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final S3PresignedService s3PresignedService;
    private final AttachmentService attachmentService;

    public FileController(S3PresignedService s3PresignedService, AttachmentService attachmentService) {
        this.s3PresignedService = s3PresignedService;
        this.attachmentService = attachmentService;
    }

    @PostMapping("/presigned-url")
    public ResponseEntity<PresignedPutUrlResponse> createPresignedUrl(
            @RequestBody PresignedPutUrlRequest request
    ) {
        var presigned = s3PresignedService.generatePutPresignedUrl(request.fileName(), request.contentType());
        return ResponseEntity.ok(new PresignedPutUrlResponse(presigned.url(), presigned.key()));
    }

    @PostMapping("/complete")
    public ResponseEntity<AttachmentCompleteResponse> complete(
            @RequestBody AttachmentCompleteRequest request,
            @RequestHeader("X-Member-Id") Long ownerMemberId
    ) {
        return ResponseEntity.ok(attachmentService.complete(request, ownerMemberId));
    }

    @PostMapping("/{attachmentId}/presigned-download")
    public ResponseEntity<PresignedDownloadResponse> presignDownload(
            @PathVariable Long attachmentId,
            @RequestHeader("X-Member-Id") Long requesterId,
            @RequestHeader(value = "X-Admin", defaultValue = "false") boolean isAdmin
    ) {
        return ResponseEntity.ok(attachmentService.presignDownload(attachmentId, requesterId, isAdmin));
    }
}