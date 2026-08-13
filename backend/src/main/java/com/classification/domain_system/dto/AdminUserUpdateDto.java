package com.classification.domain_system.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class AdminUserUpdateDto {
    private String role;
    private UUID organizationId;
    private UUID departmentId;
    private UUID teamId;
    private Boolean isActive;
    private String email;
}
