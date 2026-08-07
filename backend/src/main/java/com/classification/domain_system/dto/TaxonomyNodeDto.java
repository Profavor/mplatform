package com.classification.domain_system.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;

@Data
public class TaxonomyNodeDto {
    private UUID id;
    private UUID parentId;
    private UUID axisId;
    private Map<String, String> name;
    private String path;
    private Integer depth;
    private Integer order;
    private String icon;
    private List<TaxonomyNodeDto> children = new ArrayList<>();
}
