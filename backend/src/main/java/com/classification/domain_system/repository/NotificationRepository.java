package com.classification.domain_system.repository;

import com.classification.domain_system.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    long countByUserIdAndIsReadFalse(String userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.isRead = false")
    int markAllAsRead(@Param("userId") String userId);

    java.util.List<Notification> findByLinkUrlContainingAndIsReadFalse(String linkUrlPart);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.userId = :userId")
    int deleteByUserId(@Param("userId") String userId);
}
