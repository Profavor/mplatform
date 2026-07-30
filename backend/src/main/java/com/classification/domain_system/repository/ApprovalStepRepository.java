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
}
