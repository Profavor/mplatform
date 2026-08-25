package com.classification.domain_system.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecordLayoutDto {
    private String activeLayoutId;

    @Builder.Default
    private List<Map<String, Object>> layouts = new ArrayList<>();

    @Builder.Default
    private Integer cols = 12;

    @Builder.Default
    private Integer rowHeight = 42;

    @Builder.Default
    private List<Map<String, Object>> widgets = new ArrayList<>();

    @Builder.Default
    private Map<String, Object> options = new HashMap<>();
}
