package com.classification.domain_system.dto;

import com.classification.domain_system.entity.CodeDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeDetailResponse {
    private UUID id;
    private UUID groupId;
    private String detailCode;
    private Map<String, String> name;
    private Integer sortOrder;
    private LocalDate validFrom;
    private LocalDate validTo;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CodeDetailResponse from(CodeDetail detail) {
        if (detail == null) {
            return null;
        }
        return CodeDetailResponse.builder()
                .id(detail.getId())
                .groupId(detail.getCodeGroup() != null ? detail.getCodeGroup().getId() : null)
                .detailCode(detail.getDetailCode())
                .name(detail.getName())
                .sortOrder(detail.getSortOrder())
                .validFrom(detail.getValidFrom())
                .validTo(detail.getValidTo())
                .isActive(detail.getIsActive())
                .createdAt(detail.getCreatedAt())
                .updatedAt(detail.getUpdatedAt())
                .build();
    }
}
