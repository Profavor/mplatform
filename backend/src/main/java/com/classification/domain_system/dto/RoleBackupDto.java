package com.classification.domain_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleBackupDto {
    private String name;
    private String displayName;
    private String description;
    private Set<String> permissions;
    private Boolean isSystemRole;
}
