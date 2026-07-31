package com.classification.domain_system.repository;

import com.classification.domain_system.entity.ApprovalStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface ApprovalStepRepository extends JpaRepository<ApprovalStep, UUID> {
    Page<ApprovalStep> findByAssigneeIdAndStatus(String assigneeId, String status, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT s FROM ApprovalStep s WHERE ((s.assigneeId = :assigneeId) OR (:userRole IS NOT NULL AND s.assigneeRole = :userRole)) AND s.status = :status ORDER BY s.approvalRequest.createdAt DESC")
    Page<ApprovalStep> findMyPendingSteps(@org.springframework.data.repository.query.Param("assigneeId") String assigneeId, @org.springframework.data.repository.query.Param("userRole") String userRole, @org.springframework.data.repository.query.Param("status") String status, Pageable pageable);
}
