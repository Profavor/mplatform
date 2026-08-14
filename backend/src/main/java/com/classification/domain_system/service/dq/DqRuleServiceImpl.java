package com.classification.domain_system.service.dq;

import com.classification.domain_system.dto.DqRuleRequest;
import com.classification.domain_system.dto.DqRuleResponse;
import com.classification.domain_system.entity.DqRule;
import com.classification.domain_system.entity.DqRuleType;
import com.classification.domain_system.entity.DqSeverity;
import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.repository.DqRuleRepository;
import com.classification.domain_system.repository.FieldDefinitionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DqRuleServiceImpl implements DqRuleService {

    private final DqRuleRepository dqRuleRepository;
    private final FieldDefinitionRepository fieldDefinitionRepository;

    @Override
    public List<DqRuleResponse> getRulesByField(UUID fieldId) {
        List<DqRule> rules = dqRuleRepository.findByFieldDefinition_IdOrderBySortOrderAsc(fieldId);
        return rules.stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public DqRuleResponse createRule(UUID fieldId, DqRuleRequest request) {
        FieldDefinition field = fieldDefinitionRepository.findById(fieldId)
                .orElseThrow(() -> new RuntimeException("Field not found: " + fieldId));

        DqRule rule = new DqRule();
        rule.setFieldDefinition(field);

        UUID domainId = request.getDomainId();
        if (domainId == null && field.getDomain() != null) {
            domainId = field.getDomain().getId();
        }
        if (domainId == null && field.getDefinedAtNode() != null && field.getDefinedAtNode().getDomain() != null) {
            domainId = field.getDefinedAtNode().getDomain().getId();
        }
        if (domainId == null && field.getFieldGroup() != null) {
            if (field.getFieldGroup().getDomain() != null) {
                domainId = field.getFieldGroup().getDomain().getId();
            } else if (field.getFieldGroup().getSector() != null && field.getFieldGroup().getSector().getDomain() != null) {
                domainId = field.getFieldGroup().getSector().getDomain().getId();
            }
        }
        rule.setDomainId(domainId);

        UUID nodeId = request.getNodeId();
        if (nodeId == null && field.getDefinedAtNode() != null) {
            nodeId = field.getDefinedAtNode().getId();
        }
        rule.setNodeId(nodeId);

        rule.setRuleType(DqRuleType.valueOf(request.getRuleType()));
        rule.setSeverity(request.getSeverity() != null
                ? DqSeverity.valueOf(request.getSeverity()) : DqSeverity.ERROR);
        rule.setParams(request.getParams());
        rule.setMessage(request.getMessage());
        rule.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        rule.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        DqRule saved = dqRuleRepository.save(rule);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public DqRuleResponse updateRule(UUID ruleId, DqRuleRequest request) {
        DqRule rule = dqRuleRepository.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("DQ Rule not found: " + ruleId));

        if (request.getRuleType() != null) {
            rule.setRuleType(DqRuleType.valueOf(request.getRuleType()));
        }
        if (request.getSeverity() != null) {
            rule.setSeverity(DqSeverity.valueOf(request.getSeverity()));
        }
        if (request.getParams() != null) {
            rule.setParams(request.getParams());
        }
        if (request.getMessage() != null) {
            rule.setMessage(request.getMessage());
        }
        if (request.getSortOrder() != null) {
            rule.setSortOrder(request.getSortOrder());
        }
        if (request.getIsActive() != null) {
            rule.setIsActive(request.getIsActive());
        }

        DqRule saved = dqRuleRepository.save(rule);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteRule(UUID ruleId) {
        if (!dqRuleRepository.existsById(ruleId)) {
            throw new RuntimeException("DQ Rule not found: " + ruleId);
        }
        dqRuleRepository.deleteById(ruleId);
    }

    private DqRuleResponse toResponse(DqRule rule) {
        return DqRuleResponse.builder()
                .id(rule.getId())
                .fieldDefinitionId(rule.getFieldDefinition() != null ? rule.getFieldDefinition().getId() : null)
                .fieldKey(rule.getFieldDefinition() != null ? rule.getFieldDefinition().getKey() : null)
                .domainId(rule.getDomainId())
                .nodeId(rule.getNodeId())
                .ruleType(rule.getRuleType() != null ? rule.getRuleType().name() : null)
                .severity(rule.getSeverity() != null ? rule.getSeverity().name() : null)
                .params(rule.getParams())
                .message(rule.getMessage())
                .sortOrder(rule.getSortOrder())
                .isActive(rule.getIsActive())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .build();
    }
}
