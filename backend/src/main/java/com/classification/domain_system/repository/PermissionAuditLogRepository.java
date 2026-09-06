package com.classification.domain_system.repository;

import com.classification.domain_system.entity.PermissionAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PermissionAuditLogRepository extends JpaRepository<PermissionAuditLog, UUID> {

    Page<PermissionAuditLog> findByTargetUserIdOrderByChangedAtDesc(String targetUserId, Pageable pageable);

    Page<PermissionAuditLog> findAllByOrderByChangedAtDesc(Pageable pageable);
}
