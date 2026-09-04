package com.classification.domain_system.service;

import com.classification.domain_system.entity.WorkflowConfig;
import com.classification.domain_system.exception.BusinessException;
import com.classification.domain_system.exception.ErrorCode;
import com.classification.domain_system.repository.WorkflowConfigRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkflowConfigService {

    private final WorkflowConfigRepository repository;
    private final ObjectMapper mapper = new ObjectMapper();

    public void sanitizeJsonFields(WorkflowConfig config) {
        if (config == null) return;

        if (config.getName() != null && !config.getName().isBlank()) {
            String trimmed = config.getName().trim();
            if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
                try {
                    java.util.Map<String, String> map = java.util.Map.of("ko", trimmed, "en", trimmed);
                    config.setName(mapper.writeValueAsString(map));
                } catch (Exception ignored) {}
            }
        }

        if (config.getDescription() != null) {
            String trimmed = config.getDescription().trim();
            if (trimmed.isBlank()) {
                config.setDescription(null);
            } else if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
                try {
                    java.util.Map<String, String> map = java.util.Map.of("ko", trimmed, "en", trimmed);
                    config.setDescription(mapper.writeValueAsString(map));
                } catch (Exception ignored) {}
            }
        }
    }

    public void validateWorkflowConfig(WorkflowConfig config) {
        if (config == null) return;
        sanitizeJsonFields(config);
        if (config.getStepsConfig() == null || config.getStepsConfig().isBlank()) {
            return;
        }
        try {
            JsonNode root = mapper.readTree(config.getStepsConfig());
            if (root.has("steps") && root.get("steps").isArray() && root.get("steps").size() > 0) {
                List<Integer> orders = new ArrayList<>();
                for (JsonNode stepNode : root.get("steps")) {
                    if (stepNode.has("stepOrder")) {
                        orders.add(stepNode.get("stepOrder").asInt());
                    }
                }
                Collections.sort(orders);
                if (orders.isEmpty() || orders.get(0) != 1) {
                    throw new BusinessException(
                            ErrorCode.INVALID_WORKFLOW_CONFIG,
                            "Step orders must start at 1");
                }
                for (int i = 0; i < orders.size(); i++) {
                    if (orders.get(i) != i + 1) {
                        throw new BusinessException(
                                ErrorCode.INVALID_WORKFLOW_CONFIG,
                                "Step orders must be contiguous with no gaps");
                    }
                }
            }
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.INVALID_WORKFLOW_CONFIG,
                    "Invalid stepsConfig JSON format");
        }
    }

    @Transactional(readOnly = true)
    public List<WorkflowConfig> getAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<WorkflowConfig> getByDomain(UUID domainId) {
        return repository.findByDomainId(domainId).stream().filter(c -> c.getNodeId() == null).toList();
    }

    @Transactional(readOnly = true)
    public List<WorkflowConfig> getAllByDomain(UUID domainId) {
        return repository.findByDomainId(domainId).stream().filter(c -> c.getNodeId() == null).toList();
    }

    @Transactional(readOnly = true)
    public List<WorkflowConfig> getByNode(UUID nodeId) {
        return repository.findByNodeId(nodeId);
    }

    @Transactional
    public List<WorkflowConfig> saveForDomain(UUID domainId, List<WorkflowConfig> configs) {
        configs.forEach(this::validateWorkflowConfig);
        List<WorkflowConfig> existing = repository.findByDomainId(domainId).stream().filter(c -> c.getNodeId() == null).toList();
        repository.deleteAll(existing);

        configs.forEach(c -> {
            c.setDomainId(domainId);
            c.setNodeId(null);
            c.setId(null); // Force new
        });
        return repository.saveAll(configs);
    }

    @Transactional
    public List<WorkflowConfig> saveForNode(UUID nodeId, List<WorkflowConfig> configs) {
        configs.forEach(this::validateWorkflowConfig);
        List<WorkflowConfig> existing = repository.findByNodeId(nodeId);
        repository.deleteAll(existing);

        configs.forEach(c -> {
            c.setNodeId(nodeId);
            c.setId(null);
        });
        return repository.saveAll(configs);
    }

    @Transactional(readOnly = true)
    public Page<WorkflowConfig> getPage(int page, int size, String actionType, UUID domainId, UUID nodeId, String query) {
        Pageable pageable = PageRequest.of(
                page, size, Sort.by(Sort.Direction.DESC, "createdAt")
        );

        List<WorkflowConfig> all = repository.findAll();
        List<WorkflowConfig> filtered = all.stream()
                .filter(c -> actionType == null || actionType.isBlank() || actionType.equalsIgnoreCase("ALL") || actionType.equalsIgnoreCase(c.getActionType()))
                .filter(c -> domainId == null || domainId.equals(c.getDomainId()))
                .filter(c -> nodeId == null || (c.getNodeId() != null && nodeId.equals(c.getNodeId())))
                .filter(c -> {
                    if (query == null || query.isBlank()) return true;
                    String q = query.toLowerCase();
                    boolean matchName = c.getName() != null && c.getName().toLowerCase().contains(q);
                    boolean matchDesc = c.getDescription() != null && c.getDescription().toLowerCase().contains(q);
                    boolean matchAction = c.getActionType() != null && c.getActionType().toLowerCase().contains(q);
                    return matchName || matchDesc || matchAction;
                })
                .toList();

        int start = Math.min((int) pageable.getOffset(), filtered.size());
        int end = Math.min((start + pageable.getPageSize()), filtered.size());
        List<WorkflowConfig> pagedList = filtered.subList(start, end);

        return new PageImpl<>(pagedList, pageable, filtered.size());
    }

    @Transactional
    public WorkflowConfig saveSingle(WorkflowConfig config) {
        validateWorkflowConfig(config);
        if (config.getId() != null) {
            WorkflowConfig existing = repository.findById(config.getId()).orElse(null);
            if (existing != null) {
                existing.setName(config.getName());
                existing.setDescription(config.getDescription());
                existing.setActionType(config.getActionType());
                existing.setDomainId(config.getDomainId());
                existing.setNodeId(config.getNodeId());
                existing.setIsDefault(config.getIsDefault());
                existing.setIsActive(config.getIsActive());
                existing.setStepsConfig(config.getStepsConfig());
                return repository.save(existing);
            }
        }
        return repository.save(config);
    }

    @Transactional
    public void deleteWorkflowConfig(UUID id) {
        repository.deleteById(id);
    }
}
