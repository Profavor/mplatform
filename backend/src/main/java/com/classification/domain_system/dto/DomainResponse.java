package com.classification.domain_system.dto;

import com.classification.domain_system.entity.Domain;
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
public class DomainResponse {
    private UUID id;
    private UUID organizationId;
    private Map<String, String> name;
    private Map<String, String> description;
    private UUID identifierFieldId;
    private UUID displayNameFieldId;
    private UUID descriptionFieldId;
    private UUID imageFieldId;
    private String icon;
    private Integer sortOrder;
    private String numberingPattern;
    private boolean autoDqScanEnabled;
    private Long currentSequence;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static DomainResponse from(Domain domain) {
        if (domain == null) {
            return null;
        }
        return DomainResponse.builder()
                .id(domain.getId())
                .organizationId(domain.getOrganizationId())
                .name(domain.getName())
                .description(domain.getDescription())
                .identifierFieldId(domain.getIdentifierFieldId())
                .displayNameFieldId(domain.getDisplayNameFieldId())
                .descriptionFieldId(domain.getDescriptionFieldId())
                .imageFieldId(domain.getImageFieldId())
                .icon(domain.getIcon())
                .sortOrder(domain.getSortOrder())
                .numberingPattern(domain.getNumberingPattern())
                .autoDqScanEnabled(domain.isAutoDqScanEnabled())
                .currentSequence(domain.getCurrentSequence())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
