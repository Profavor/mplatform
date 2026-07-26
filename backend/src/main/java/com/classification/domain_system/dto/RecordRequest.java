package com.classification.domain_system.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class RecordRequest {
    private String data; // JSON string representing record payload
    private UUID requesterId; 
    private String comment; // Draft comment
    private UUID workflowConfigId;
}
