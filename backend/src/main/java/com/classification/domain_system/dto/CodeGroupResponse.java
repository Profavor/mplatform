package com.classification.domain_system.dto;

import com.classification.domain_system.entity.CodeGroup;
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
public class CodeGroupResponse {
    private UUID id;
    private String groupCode;
    private Map<String, String> name;
    private Map<String, String> description;
    private UUID organizationId;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CodeGroupResponse from(CodeGroup group) {
        if (group == null) {
            return null;
        }
        return CodeGroupResponse.builder()
                .id(group.getId())
                .groupCode(group.getGroupCode())
                .name(group.getName())
                .description(group.getDescription())
                .organizationId(group.getOrganizationId())
                .isActive(group.getIsActive())
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .build();
    }
}
