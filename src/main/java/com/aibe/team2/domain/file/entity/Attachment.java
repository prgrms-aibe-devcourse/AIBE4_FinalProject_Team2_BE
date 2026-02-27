package com.aibe.team2.domain.file.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "attachment",
        indexes = {
                @Index(name = "idx_attachment_owner", columnList = "owner_member_id"),
                @Index(name = "idx_attachment_target", columnList = "target_type,target_id")
        })
public class Attachment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_member_id", nullable = false)
    private Long ownerMemberId;

    @Column(name = "s3_key", nullable = false, length = 500)
    private String s3Key;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false, length = 50)
    private AttachmentFileType fileType;

    @Column(name = "target_type", nullable = false, length = 50)
    private String targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Builder
    private Attachment(Long ownerMemberId, String s3Key, AttachmentFileType fileType, String targetType, Long targetId) {
        this.ownerMemberId = ownerMemberId;
        this.s3Key = s3Key;
        this.fileType = fileType;
        this.targetType = targetType;
        this.targetId = targetId;
    }
}