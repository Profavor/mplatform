package com.classification.domain_system.dto;

import com.classification.domain_system.entity.FieldDefinition;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FieldDefinitionResponse {

    private UUID id;
    private UUID domainId;
    private UUID definedAtNodeId;
    private Map<String, String> name;
    private Map<String, String> hint;
    private FieldGroupDto fieldGroup;
    private String key;
    private String type;
    private String unit;
    private String options;
    private Boolean required;
    private String defaultValue;
    private Integer order;
    private Integer gridWidth;
    private Integer tableColumnWidth;
    private Boolean isHighlighted;
    private Boolean isRemoved;
    private Boolean isMultiValue;
    private Boolean isTable;
    private Boolean isEncrypted;
    private Boolean isSearchable;
    private Boolean isReadOnly;
    private Boolean isImmutable;
    private Boolean isHidden;
    private String maskingPattern;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String approvalStatus;
    private Boolean isPendingApproval;
    private UUID approvalRequestId;

    public static FieldDefinitionResponse from(FieldDefinition entity) {
        if (entity == null) return null;

        FieldGroupDto groupDto = null;
        if (entity.getFieldGroup() != null) {
            groupDto = new FieldGroupDto();
            groupDto.setId(entity.getFieldGroup().getId());
            groupDto.setName(entity.getFieldGroup().getName());
            groupDto.setSortOrder(entity.getFieldGroup().getSortOrder());
            groupDto.setIsDefaultOpen(entity.getFieldGroup().getIsDefaultOpen());
            groupDto.setSectorId(entity.getFieldGroup().getSectorId());
            if (entity.getFieldGroup().getSector() != null) {
                SectorDto sectorDto = new SectorDto();
                sectorDto.setId(entity.getFieldGroup().getSector().getId());
                sectorDto.setName(entity.getFieldGroup().getSector().getName());
                sectorDto.setSortOrder(entity.getFieldGroup().getSector().getSortOrder());
                try {
                    if (entity.getFieldGroup().getSector().getDomain() != null) {
                        sectorDto.setDomainId(entity.getFieldGroup().getSector().getDomain().getId());
                    } else if (entity.getDomainId() != null) {
                        sectorDto.setDomainId(entity.getDomainId());
                    }
                } catch (Exception ignored) {
                    sectorDto.setDomainId(entity.getDomainId());
                }
                groupDto.setSector(sectorDto);
            }
        }

        return FieldDefinitionResponse.builder()
                .id(entity.getId())
                .domainId(entity.getDomainId())
                .definedAtNodeId(entity.getDefinedAtNodeId())
                .name(entity.getName())
                .hint(entity.getHint())
                .fieldGroup(groupDto)
                .key(entity.getKey())
                .type(entity.getType())
                .unit(entity.getUnit())
                .options(entity.getOptions())
                .required(entity.getRequired())
                .defaultValue(entity.getDefaultValue())
                .order(entity.getOrder())
                .gridWidth(entity.getGridWidth())
                .tableColumnWidth(entity.getTableColumnWidth())
                .isHighlighted(entity.getIsHighlighted())
                .isRemoved(entity.getIsRemoved())
                .isMultiValue(entity.getIsMultiValue())
                .isTable(entity.getIsTable())
                .isEncrypted(entity.getIsEncrypted())
                .isSearchable(entity.getIsSearchable())
                .isReadOnly(entity.getIsReadOnly())
                .isImmutable(entity.getIsImmutable())
                .isHidden(entity.getIsHidden())
                .maskingPattern(entity.getMaskingPattern())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .approvalStatus(entity.getApprovalStatus())
                .isPendingApproval(entity.getIsPendingApproval())
                .approvalRequestId(entity.getApprovalRequestId())
                .build();
    }
}
