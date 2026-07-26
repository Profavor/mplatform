package com.classification.domain_system.repository;

import com.classification.domain_system.entity.WorkflowConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;
import java.util.Optional;

public interface WorkflowConfigRepository extends JpaRepository<WorkflowConfig, UUID> {
    List<WorkflowConfig> findByNodeIdAndActionType(UUID nodeId, String actionType);
    List<WorkflowConfig> findByDomainIdAndNodeIdIsNullAndActionType(UUID domainId, String actionType);
    List<WorkflowConfig> findByDomainId(UUID domainId);
    List<WorkflowConfig> findByNodeId(UUID nodeId);
}
