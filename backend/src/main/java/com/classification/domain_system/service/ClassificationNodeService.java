package com.classification.domain_system.service;

import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.DomainRepository;
import com.classification.domain_system.dto.ClassificationNodeRequest;
import com.classification.domain_system.repository.WorkflowConfigRepository;
import com.classification.domain_system.repository.ApprovalRequestRepository;
import com.classification.domain_system.entity.WorkflowConfig;
import com.classification.domain_system.entity.ApprovalRequest;
import com.classification.domain_system.exception.BusinessException;
import com.classification.domain_system.exception.ErrorCode;
import com.classification.domain_system.exception.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClassificationNodeService {
    
    private final ClassificationNodeRepository nodeRepository;
    private final DomainRepository domainRepository;

    @Autowired(required = false)
    private WorkflowConfigRepository workflowConfigRepository;

    @Autowired(required = false)
    private SchemaHistoryService schemaHistoryService;

    @Autowired(required = false)
    private ApprovalRequestRepository approvalRequestRepository;

    @Autowired(required = false)
    private ApplicationEventPublisher eventPublisher;

    @Lazy
    @Autowired(required = false)
    private ApprovalService approvalService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule()).disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private boolean hasSchemaApproval(UUID domainId) {
        if (workflowConfigRepository == null || domainId == null) return false;
        try {
            return workflowConfigRepository.findByDomainIdAndNodeIdIsNullAndActionType(domainId, "SCHEMA_CHANGE").isPresent();
        } catch (Exception e) {
            return false;
        }
    }

    private void recordSchemaChange(UUID domainId, String targetType, UUID targetId, String action, Object beforeData, Object afterData) {
        if (schemaHistoryService != null && domainId != null) {
            schemaHistoryService.recordChange(domainId, targetType, targetId, action, beforeData, afterData, "SYSTEM");
        }
    }

    @Transactional
    public ClassificationNode createNodeDirect(UUID domainId, ClassificationNodeRequest request) {
        Domain domain = domainRepository.findById(domainId)
                .orElseThrow(() -> new ResourceNotFoundException("Domain not found with id: " + domainId));
                
        ClassificationNode parent = null;
        String domainNameStr = domain.getName() != null ? domain.getName().getOrDefault("ko", domain.getName().get("en")) : "domain";
        String path = "/" + domainNameStr;
        int depth = 0;
        
        String reqNameStr = request.getName() != null ? request.getName().getOrDefault("ko", request.getName().get("en")) : "node";

        if (request.getParentId() != null) {
            parent = nodeRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent node not found with id: " + request.getParentId()));
            path = parent.getPath() + "/" + reqNameStr;
            depth = parent.getDepth() + 1;
        } else {
            path = path + "/" + reqNameStr;
            depth = 1;
        }
        
        ClassificationNode node = new ClassificationNode();
        node.setDomain(domain);
        node.setParent(parent);
        node.setName(request.getName());
        node.setPath(path);
        node.setDepth(depth);
        node.setOrder(request.getOrder() != null ? request.getOrder() : 0);
        node.setIcon(request.getIcon());
        node.setIsDeleted(false);
        
        ClassificationNode savedNode = nodeRepository.save(node);
        recordSchemaChange(domainId, "NODE", savedNode.getId(), "CREATE", null, savedNode);
        return savedNode;
    }

    @Transactional
    public ClassificationNode createNode(UUID domainId, ClassificationNodeRequest request) {
        Domain domain = domainRepository.findById(domainId)
                .orElseThrow(() -> new ResourceNotFoundException("Domain not found with id: " + domainId));

        if (hasSchemaApproval(domainId)) {
            try {
                java.util.Map<String, Object> changesMap = new java.util.HashMap<>();
                changesMap.put("domainId", domainId);
                changesMap.put("request", request);
                ClassificationNode pendingNode = new ClassificationNode();
                pendingNode.setId(UUID.randomUUID());
                return pendingNode;
            } catch (Exception e) {
                throw new RuntimeException("Failed to create schema approval request", e);
            }
        }

        return createNodeDirect(domainId, request);
    }
    
    @Transactional(readOnly = true)
    public List<ClassificationNode> getTree(UUID domainId) {
        return nodeRepository.findByDomain_IdAndParentIsNullAndIsDeletedFalseOrderByOrderAsc(domainId);
    }
    
    @Transactional
    public ClassificationNode updateNodeDirect(UUID domainId, UUID nodeId, ClassificationNodeRequest request) {
        ClassificationNode node = nodeRepository.findById(nodeId)
                .orElseThrow(() -> new ResourceNotFoundException("Node not found with id: " + nodeId));
                
        if (!node.getDomain().getId().equals(domainId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "Node does not belong to specified domain");
        }
        
        ClassificationNode beforeState = new ClassificationNode();
        beforeState.setId(node.getId());
        beforeState.setName(node.getName());
        beforeState.setPath(node.getPath());

        node.setName(request.getName());
        node.setOrder(request.getOrder() != null ? request.getOrder() : node.getOrder());
        node.setIcon(request.getIcon() != null ? request.getIcon() : node.getIcon());
        
        ClassificationNode savedNode = nodeRepository.save(node);
        recordSchemaChange(domainId, "NODE", nodeId, "UPDATE", beforeState, savedNode);
        return savedNode;
    }

    @Transactional
    public ClassificationNode updateNode(UUID domainId, UUID nodeId, ClassificationNodeRequest request) {
        ClassificationNode node = nodeRepository.findById(nodeId)
                .orElseThrow(() -> new ResourceNotFoundException("Node not found with id: " + nodeId));

        if (!node.getDomain().getId().equals(domainId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "Node does not belong to specified domain");
        }

        if (hasSchemaApproval(domainId)) {
            try {
                java.util.Map<String, Object> changesMap = new java.util.HashMap<>();
                changesMap.put("domainId", domainId);
                changesMap.put("nodeId", nodeId);
                changesMap.put("request", request);
                return node;
            } catch (Exception e) {
                throw new RuntimeException("Failed to create schema approval request", e);
            }
        }

        return updateNodeDirect(domainId, nodeId, request);
    }
}
