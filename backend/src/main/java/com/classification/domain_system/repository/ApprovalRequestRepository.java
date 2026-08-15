package com.classification.domain_system.repository;

import com.classification.domain_system.entity.ApprovalRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

@Repository
public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, UUID>, JpaSpecificationExecutor<ApprovalRequest> {
    Page<ApprovalRequest> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
    List<ApprovalRequest> findByStatus(String status);
    Page<ApprovalRequest> findByRequesterIdOrderByCreatedAtDesc(String requesterId, Pageable pageable);

    @Query("SELECT r FROM ApprovalRequest r WHERE (r.requesterId = :requesterId OR r.requesterId = :username OR r.requesterId NOT IN (SELECT u.id FROM User u)) ORDER BY r.createdAt DESC")
    Page<ApprovalRequest> findMyRequestsForUser(@Param("requesterId") String requesterId, @Param("username") String username, Pageable pageable);
    List<ApprovalRequest> findByTargetIdAndStatus(UUID targetId, String status);
    List<ApprovalRequest> findByTargetIdOrderByCreatedAtAsc(UUID targetId);
    List<ApprovalRequest> findByCreatedAtAfter(java.time.LocalDateTime createdAt);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM ApprovalRequest a WHERE a.id = :id")
    Optional<ApprovalRequest> findByIdWithLock(@Param("id") UUID id);
}
