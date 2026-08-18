package com.classification.domain_system.repository;

import com.classification.domain_system.entity.InboxRecipient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface InboxRecipientRepository extends JpaRepository<InboxRecipient, UUID> {
    Page<InboxRecipient> findByUserIdAndFolderAndIsDeletedFalse(String userId, String folder, Pageable pageable);
    Page<InboxRecipient> findByUserIdAndIsStarredTrueAndIsDeletedFalse(String userId, Pageable pageable);
    long countByUserIdAndIsStarredTrueAndIsReadFalseAndIsDeletedFalse(String userId);
    long countByUserIdAndFolderAndIsReadFalseAndIsDeletedFalse(String userId, String folder);
    long countByUserIdAndIsReadFalseAndIsDeletedFalseAndFolderNot(String userId, String folder);
    List<InboxRecipient> findByUserIdAndMessageId(String userId, UUID messageId);
    Optional<InboxRecipient> findFirstByUserIdAndMessageIdAndFolder(String userId, UUID messageId, String folder);
    Optional<InboxRecipient> findFirstByUserIdAndMessageId(String userId, UUID messageId);
    List<InboxRecipient> findByUserIdAndMessageIdIn(String userId, List<UUID> messageIds);
    List<InboxRecipient> findByMessageId(UUID messageId);
    
    @Query("SELECT r FROM InboxRecipient r JOIN r.message m WHERE r.userId = :userId AND r.folder = :folder AND r.isDeleted = false AND (LOWER(m.subject) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(m.body) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<InboxRecipient> searchByKeyword(@Param("userId") String userId, @Param("folder") String folder, @Param("keyword") String keyword, Pageable pageable);
}
