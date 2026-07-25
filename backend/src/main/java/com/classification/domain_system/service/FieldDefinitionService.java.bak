package com.classification.domain_system.service;

import com.classification.domain_system.entity.FieldDefinition;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.repository.FieldDefinitionRepository;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.DomainRepository;
import com.classification.domain_system.dto.FieldDefinitionRequest;
import com.classification.domain_system.entity.Domain;
import com.classification.domain_system.repository.FieldGroupRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FieldDefinitionService {
    
    private final FieldDefinitionRepository fieldRepository;
    private final ClassificationNodeRepository nodeRepository;
    private final DomainRepository domainRepository;
    private final FieldGroupRepository fieldGroupRepository;
    private final JdbcTemplate jdbcTemplate;
    
    private void manageIndex(String fieldKey, boolean isSearchable) {
        if (fieldKey == null || fieldKey.trim().isEmpty()) return;
        String safeKey = fieldKey.replaceAll("[^a-zA-Z0-9_]", "_");
        String indexName = "idx_record_search_" + safeKey;
        if (isSearchable) {
            String sql = "CREATE INDEX IF NOT EXISTS " + indexName + " ON record ((data->>'" + safeKey + "'))";
            try {
                jdbcTemplate.execute(sql);
            } catch (Exception e) {
                System.err.println("Failed to create index: " + e.getMessage());
            }
        } else {
            String sql = "DROP INDEX IF EXISTS " + indexName;
            try {
                jdbcTemplate.execute(sql);
            } catch (Exception e) {
                System.err.println("Failed to drop index: " + e.getMessage());
            }
        }
    }
    
    private void populateFieldProperties(FieldDefinition field, FieldDefinitionRequest request, boolean isUpdate) {
        field.setName(request.getName());
        
        if (request.getFieldGroupId() != null) {
            field.setFieldGroup(fieldGroupRepository.findById(request.getFieldGroupId())
                .orElseThrow(() -> new RuntimeException("FieldGroup not found")));
        } else {
            field.setFieldGroup(null);
        }
        
        field.setKey(request.getKey());
        field.setType(request.getType());
        
        if (isUpdate) {
            field.setUnit(request.getUnit() != null ? request.getUnit() : field.getUnit());
            field.setGridWidth(request.getGridWidth() != null ? request.getGridWidth() : field.getGridWidth());
            field.setTableColumnWidth(request.getTableColumnWidth() != null ? request.getTableColumnWidth() : field.getTableColumnWidth());
            field.setIsHighlighted(request.getIsHighlighted() != null ? request.getIsHighlighted() : field.getIsHighlighted());
        } else {
            field.setGridWidth(request.getGridWidth());
            field.setTableColumnWidth(request.getTableColumnWidth());
            field.setIsHighlighted(request.getIsHighlighted() != null ? request.getIsHighlighted() : false);
            field.setIsRemoved(false);
        }
        
        field.setOptions(normalizeJsonStr(request.getOptions()));
        field.setDefaultValue(normalizeJsonStr(request.getDefaultValue()));
        
        field.setRequired(request.getRequired() != null ? request.getRequired() : (isUpdate ? field.getRequired() : false));
        field.setOrder(request.getOrder() != null ? request.getOrder() : (isUpdate ? field.getOrder() : 0));
        field.setIsMultiValue(request.getIsMultiValue() != null ? request.getIsMultiValue() : (isUpdate ? field.getIsMultiValue() : false));
        field.setIsTable(request.getIsTable() != null ? request.getIsTable() : (isUpdate ? field.getIsTable() : false));
        field.setIsEncrypted(request.getIsEncrypted() != null ? request.getIsEncrypted() : (isUpdate ? field.getIsEncrypted() : false));
        field.setIsReadOnly(request.getIsReadOnly() != null ? request.getIsReadOnly() : (isUpdate ? field.getIsReadOnly() : false));
        field.setIsImmutable(request.getIsImmutable() != null ? request.getIsImmutable() : (isUpdate ? field.getIsImmutable() : false));
        field.setIsHidden(request.getIsHidden() != null ? request.getIsHidden() : (isUpdate ? field.getIsHidden() : false));
    }
    
    @Transactional
    public FieldDefinition addField(UUID nodeId, FieldDefinitionRequest request) {
        ClassificationNode node = nodeRepository.findById(nodeId)
                .orElseThrow(() -> new RuntimeException("Node not found"));
                
        FieldDefinition field = new FieldDefinition();
        field.setDefinedAtNode(node);
        populateFieldProperties(field, request, false);
        field.setIsSearchable(request.getIsSearchable() != null ? request.getIsSearchable() : false);
        
        FieldDefinition savedField = fieldRepository.save(field);
        if (Boolean.TRUE.equals(request.getIsSearchable())) {
            manageIndex(savedField.getKey(), true);
        }
        return savedField;
    }
    
    @Transactional
    public FieldDefinition addDomainField(UUID domainId, FieldDefinitionRequest request) {
        Domain domain = domainRepository.findById(domainId)
                .orElseThrow(() -> new RuntimeException("Domain not found"));
                
        FieldDefinition field = new FieldDefinition();
        field.setDomain(domain);
        populateFieldProperties(field, request, false);
        field.setIsSearchable(request.getIsSearchable() != null ? request.getIsSearchable() : false);
        
        FieldDefinition savedField = fieldRepository.save(field);
        if (Boolean.TRUE.equals(request.getIsSearchable())) {
            manageIndex(savedField.getKey(), true);
        }
        return savedField;
    }
    
    @Transactional
    public FieldDefinition updateField(UUID nodeId, UUID fieldId, FieldDefinitionRequest request) {
        FieldDefinition field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new RuntimeException("Field not found"));
                
        if (field.getDefinedAtNode() == null || !field.getDefinedAtNode().getId().equals(nodeId)) {
            throw new RuntimeException("Field does not belong to the specified node");
        }
        
        populateFieldProperties(field, request, true);
        
        Boolean wasSearchable = field.getIsSearchable();
        Boolean willBeSearchable = request.getIsSearchable() != null ? request.getIsSearchable() : field.getIsSearchable();
        field.setIsSearchable(willBeSearchable);
        
        FieldDefinition savedField = fieldRepository.save(field);
        
        if (Boolean.TRUE.equals(willBeSearchable) && !Boolean.TRUE.equals(wasSearchable)) {
            manageIndex(savedField.getKey(), true);
        } else if (!Boolean.TRUE.equals(willBeSearchable) && Boolean.TRUE.equals(wasSearchable)) {
            manageIndex(savedField.getKey(), false);
        }
        return savedField;
    }
    
    @Transactional
    public FieldDefinition updateDomainField(UUID domainId, UUID fieldId, FieldDefinitionRequest request) {
        FieldDefinition field = fieldRepository.findById(fieldId)
                .orElseThrow(() -> new RuntimeException("Field not found"));
                
        if (field.getDomain() == null || !field.getDomain().getId().equals(domainId)) {
            throw new RuntimeException("Field does not belong to the specified domain");
        }
        
        populateFieldProperties(field, request, true);
        
        Boolean wasSearchable = field.getIsSearchable();
        Boolean willBeSearchable = request.getIsSearchable() != null ? request.getIsSearchable() : field.getIsSearchable();
        field.setIsSearchable(willBeSearchable);
        
        FieldDefinition savedField = fieldRepository.save(field);
        
        if (Boolean.TRUE.equals(willBeSearchable) && !Boolean.TRUE.equals(wasSearchable)) {
            manageIndex(savedField.getKey(), true);
        } else if (!Boolean.TRUE.equals(willBeSearchable) && Boolean.TRUE.equals(wasSearchable)) {
            manageIndex(savedField.getKey(), false);
        }
        return savedField;
    }
    
    @Transactional(readOnly = true)
    public List<FieldDefinition> getEffectiveFields(UUID nodeId) {
        if (nodeId == null) {
            return java.util.Collections.emptyList();
        }

        ClassificationNode node = nodeRepository.findById(nodeId).orElse(null);
        List<FieldDefinition> effectiveFields = new java.util.ArrayList<>();
        
        if (node != null) {
            List<FieldDefinition> domainFields = fieldRepository.findDomainFieldsWithSort(node.getDomain().getId());
            List<FieldDefinition> nodeFields = fieldRepository.findNodeFieldsWithSort(nodeId);
            
            List<UUID> pathIds = new java.util.ArrayList<>();
            ClassificationNode current = node.getParent();
            while (current != null) {
                pathIds.add(current.getId());
                current = current.getParent();
            }
                
            List<FieldDefinition> inheritedFields = fieldRepository.findByDefinedAtNode_IdIn(pathIds);
            
            effectiveFields.addAll(domainFields);
            effectiveFields.addAll(inheritedFields);
            effectiveFields.addAll(nodeFields);
        } else {
            Domain domain = domainRepository.findById(nodeId).orElse(null);
            if (domain != null) {
                effectiveFields.addAll(fieldRepository.findDomainFieldsWithSort(domain.getId()));
            }
        }

        effectiveFields.sort((f1, f2) -> {
            int s1 = f1.getFieldGroup() != null && f1.getFieldGroup().getSector() != null ? (f1.getFieldGroup().getSector().getSortOrder() != null ? f1.getFieldGroup().getSector().getSortOrder() : 9999) : 9999;
            int s2 = f2.getFieldGroup() != null && f2.getFieldGroup().getSector() != null ? (f2.getFieldGroup().getSector().getSortOrder() != null ? f2.getFieldGroup().getSector().getSortOrder() : 9999) : 9999;
            if (s1 != s2) return Integer.compare(s1, s2);
            
            int g1 = f1.getFieldGroup() != null ? (f1.getFieldGroup().getSortOrder() != null ? f1.getFieldGroup().getSortOrder() : 9999) : 9999;
            int g2 = f2.getFieldGroup() != null ? (f2.getFieldGroup().getSortOrder() != null ? f2.getFieldGroup().getSortOrder() : 9999) : 9999;
            if (g1 != g2) return Integer.compare(g1, g2);
            
            int o1 = f1.getOrder() != null ? f1.getOrder() : 9999;
            int o2 = f2.getOrder() != null ? f2.getOrder() : 9999;
            return Integer.compare(o1, o2);
        });

        return effectiveFields;
    }

    @Transactional(readOnly = true)
    public Page<FieldDefinition> getEffectiveFieldsPage(UUID nodeId, Pageable pageable) {
        ClassificationNode node = nodeRepository.findById(nodeId)
                .orElseThrow(() -> new RuntimeException("Node not found"));
        return fieldRepository.findEffectiveFieldsWithPagination(nodeId, node.getDomain().getId(), pageable);
    }
    
    @Transactional(readOnly = true)
    public List<FieldDefinition> getDomainFields(UUID domainId) {
        return fieldRepository.findDomainFieldsWithSort(domainId);
    }
    
    private String normalizeJsonStr(String val) {
        if (val == null || val.trim().isEmpty()) {
            return null;
        }
        val = val.trim();
        if (val.startsWith("{") || val.startsWith("[")) {
            return val;
        }
        String[] parts = val.split("\\s*,\\s*");
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < parts.length; i++) {
            sb.append("\"").append(parts[i].replace("\"", "\\\"")).append("\"");
            if (i < parts.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
