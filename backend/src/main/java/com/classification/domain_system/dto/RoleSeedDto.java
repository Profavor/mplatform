package com.classification.domain_system.dto;

import lombok.Data;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class RoleSeedDto {
    @JsonProperty("NAME")
    private String name;
    
    private String displayName;
    
    @JsonProperty("DESCRIPTION")
    private String description;
    
    private Boolean isSystemRole;
    private List<String> permissions;
}
