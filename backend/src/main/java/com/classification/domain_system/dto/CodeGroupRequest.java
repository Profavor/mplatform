package com.classification.domain_system.dto;

import lombok.Data;
import java.util.Map;

@Data
public class CodeGroupRequest {
    private String groupCode;
    private Map<String, String> name;
    private Map<String, String> description;
    private Boolean isActive;
}
