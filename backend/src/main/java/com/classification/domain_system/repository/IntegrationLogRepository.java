package com.classification.domain_system.repository;

import com.classification.domain_system.entity.IntegrationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface IntegrationLogRepository extends JpaRepository<IntegrationLog, UUID> {
    Page<IntegrationLog> findByChannelId(UUID channelId, Pageable pageable);
    java.util.List<IntegrationLog> findByRecordIdOrderByCreatedAtDesc(UUID recordId);
    Page<IntegrationLog> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
    java.util.List<IntegrationLog> findByStatus(String status);
    java.util.List<IntegrationLog> findByStatusAndNextRetryAtBefore(String status, java.time.LocalDateTime now);
}
