package com.classification.domain_system.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class ApprovalStepRequest {
    private String stepType; // CONSENSUS, APPROVAL
    private String assigneeId;
    private Integer stepOrder;
}
