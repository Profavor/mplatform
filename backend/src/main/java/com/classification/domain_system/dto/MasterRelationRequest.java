package com.classification.domain_system.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class MasterRelationRequest {
    private UUID sourceDomainId;
    private String sourceFieldKey;
    private UUID targetDomainId;
    private String relationType;
    private String cascadePolicy;
    private Boolean isActive;
}
