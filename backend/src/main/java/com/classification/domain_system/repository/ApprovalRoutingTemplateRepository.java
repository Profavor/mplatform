package com.classification.domain_system.repository;

import com.classification.domain_system.entity.ApprovalRoutingTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ApprovalRoutingTemplateRepository extends JpaRepository<ApprovalRoutingTemplate, UUID> {
    List<ApprovalRoutingTemplate> findByDomainId(UUID domainId);
}
