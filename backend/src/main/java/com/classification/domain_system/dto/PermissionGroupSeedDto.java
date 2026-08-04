package com.classification.domain_system.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class PermissionGroupSeedDto {
    @JsonProperty("ID")
    private String id;
    
    @JsonProperty("CODE")
    private String code;
    
    @JsonProperty("TITLE_KO")
    private String titleKo;
    
    @JsonProperty("TITLE_EN")
    private String titleEn;
    
    @JsonProperty("ICON")
    private String icon;
    
    @JsonProperty("COLOR")
    private String color;
    
    @JsonProperty("CHIP_CLASS")
    private String chipClass;
    
    @JsonProperty("SORT_ORDER")
    private Integer sortOrder;
    
    private List<PermissionItemSeedDto> items;
}
