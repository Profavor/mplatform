package com.classification.domain_system.service;

import com.classification.domain_system.entity.TaxonomyVersion;
import com.classification.domain_system.repository.TaxonomyVersionRepository;
import com.classification.domain_system.repository.ClassificationNodeRepository;
import com.classification.domain_system.repository.FieldDefinitionRepository;
import com.classification.domain_system.dto.TaxonomyNodeDto;
import com.classification.domain_system.dto.FieldDefinitionDto;
import com.classification.domain_system.entity.ClassificationNode;
import com.classification.domain_system.entity.FieldDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaxonomyVersionService {

    private final TaxonomyVersionRepository taxonomyVersionRepository;
    private final ClassificationNodeRepository classificationNodeRepository;
    private final FieldDefinitionRepository fieldDefinitionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public TaxonomyVersion createSnapshot(UUID domainId, String label, String publishedBy) {
        TaxonomyVersion version = new TaxonomyVersion();
        version.setDomainId(domainId);
        version.setVersionLabel(label);
        version.setPublishedBy(publishedBy);
        
        List<ClassificationNode> rootNodes = classificationNodeRepository.findByDomain_IdAndParentIsNullAndIsDeletedFalseOrderByOrderAsc(domainId);
        List<TaxonomyNodeDto> dtoList = rootNodes.stream().map(this::convertToDto).toList();
        
        try {
            String snapshotJson = objectMapper.writeValueAsString(dtoList);
            version.setSnapshotData(snapshotJson);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize taxonomy snapshot", e);
            version.setSnapshotData("[]");
        }
        
        return taxonomyVersionRepository.save(version);
    }

    private TaxonomyNodeDto convertToDto(ClassificationNode node) {
        TaxonomyNodeDto dto = new TaxonomyNodeDto();
        dto.setId(node.getId());
        dto.setParentId(node.getParent() != null ? node.getParent().getId() : null);
        dto.setAxisId(node.getAxisId());
        dto.setName(node.getName());
        dto.setPath(node.getPath());
        dto.setDepth(node.getDepth());
        dto.setOrder(node.getOrder());
        dto.setIcon(node.getIcon());
        
        List<FieldDefinition> fieldDefs = fieldDefinitionRepository.findNodeFieldsWithSort(node.getId());
        List<FieldDefinitionDto> fieldDefDtos = fieldDefs.stream().map(this::convertFieldDefToDto).toList();
        dto.setFieldDefinitions(fieldDefDtos);
        
        if (node.getChildren() != null && !node.getChildren().isEmpty()) {
            dto.setChildren(node.getChildren().stream()
                    .filter(c -> !c.getIsDeleted())
                    .map(this::convertToDto)
                    .toList());
        }
        return dto;
    }

    private FieldDefinitionDto convertFieldDefToDto(FieldDefinition fieldDef) {
        FieldDefinitionDto dto = new FieldDefinitionDto();
        dto.setId(fieldDef.getId());
        if (fieldDef.getDomain() != null) dto.setDomainId(fieldDef.getDomain().getId());
        if (fieldDef.getDefinedAtNode() != null) dto.setDefinedAtNodeId(fieldDef.getDefinedAtNode().getId());
        dto.setName(fieldDef.getName());
        dto.setHint(fieldDef.getHint());
        dto.setKey(fieldDef.getKey());
        dto.setType(fieldDef.getType());
        dto.setUnit(fieldDef.getUnit());
        dto.setOptions(fieldDef.getOptions());
        dto.setRequired(fieldDef.getRequired());
        dto.setDefaultValue(fieldDef.getDefaultValue());
        dto.setOrder(fieldDef.getOrder());
        dto.setGridWidth(fieldDef.getGridWidth());
        dto.setTableColumnWidth(fieldDef.getTableColumnWidth());
        dto.setIsHighlighted(fieldDef.getIsHighlighted());
        dto.setIsMultiValue(fieldDef.getIsMultiValue());
        dto.setIsTable(fieldDef.getIsTable());
        dto.setIsEncrypted(fieldDef.getIsEncrypted());
        dto.setIsSearchable(fieldDef.getIsSearchable());
        dto.setIsReadOnly(fieldDef.getIsReadOnly());
        dto.setIsImmutable(fieldDef.getIsImmutable());
        dto.setIsHidden(fieldDef.getIsHidden());
        dto.setMaskingPattern(fieldDef.getMaskingPattern());
        return dto;
    }

    @Transactional(readOnly = true)
    public List<TaxonomyVersion> getVersions(UUID domainId) {
        return taxonomyVersionRepository.findByDomainIdOrderByCreatedAtDesc(domainId);
    }

    @Transactional(readOnly = true)
    public String getSnapshotData(UUID versionId) {
        return taxonomyVersionRepository.findById(versionId)
                .map(TaxonomyVersion::getSnapshotData)
                .orElse(null);
    }
}
