package com.classification.domain_system.repository;

import com.classification.domain_system.entity.InboxAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface InboxAttachmentRepository extends JpaRepository<InboxAttachment, UUID> {
    List<InboxAttachment> findByMessageId(UUID messageId);
}
