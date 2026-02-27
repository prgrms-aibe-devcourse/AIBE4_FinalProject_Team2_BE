package com.aibe.team2.domain.file.repository;

import com.aibe.team2.domain.file.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
}