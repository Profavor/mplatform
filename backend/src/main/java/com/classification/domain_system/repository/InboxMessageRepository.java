package com.classification.domain_system.repository;

import com.classification.domain_system.entity.InboxMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InboxMessageRepository extends JpaRepository<InboxMessage, UUID> {
    Optional<InboxMessage> findByExternalMessageId(String externalMessageId);
    List<InboxMessage> findByRootMessageIdOrderByCreatedAtAsc(UUID rootMessageId);
    long countByRootMessageId(UUID rootMessageId);
}
