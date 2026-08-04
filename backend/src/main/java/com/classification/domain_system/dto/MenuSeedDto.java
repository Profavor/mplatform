package com.classification.domain_system.dto;

import lombok.Data;
import java.util.List;

@Data
public class MenuSeedDto {
    private Long id;
    private String name;
    private String path;
    private String icon;
    private Long parentId;
    private Integer sortOrder;
    private Boolean isActive;
    private List<String> requiredRoles;
}
