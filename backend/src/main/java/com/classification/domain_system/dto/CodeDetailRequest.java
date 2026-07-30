package com.classification.domain_system.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.Map;

@Data
public class CodeDetailRequest {
    private String detailCode;
    private Map<String, String> name;
    private Integer sortOrder;
    private LocalDate validFrom;
    private LocalDate validTo;
    private Boolean isActive;
}
