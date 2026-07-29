package com.classification.domain_system.dto;

import com.classification.domain_system.entity.ClassificationAxis;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Getter
@Builder
public class ClassificationAxisResponse {
    private final UUID id;
    private final UUID domainId;
    private final String axisCode;
    private final Map<String, String> name;
    private final String description;
    private final Boolean isDefault;
    private final Integer sortOrder;
    private final LocalDateTime createdAt;

    public static ClassificationAxisResponse fromEntity(ClassificationAxis axis) {
        if (axis == null) return null;
        return ClassificationAxisResponse.builder()
                .id(axis.getId())
                .domainId(axis.getDomainId())
                .axisCode(axis.getAxisCode())
                .name(axis.getName())
                .description(axis.getDescription())
                .isDefault(axis.getIsDefault())
                .sortOrder(axis.getSortOrder())
                .createdAt(axis.getCreatedAt())
                .build();
    }
}
