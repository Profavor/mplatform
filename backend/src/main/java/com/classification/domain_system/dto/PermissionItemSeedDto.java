package com.classification.domain_system.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PermissionItemSeedDto {
    @JsonProperty("LABEL_KO")
    private String labelKo;
    
    @JsonProperty("LABEL_EN")
    private String labelEn;
    
    @JsonProperty("PERM_VALUE")
    private String permValue;
    
    @JsonProperty("SORT_ORDER")
    private Integer sortOrder;
}
