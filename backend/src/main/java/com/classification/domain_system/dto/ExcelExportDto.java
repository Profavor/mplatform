package com.classification.domain_system.dto;

import lombok.*;

import java.util.List;

public class ExcelExportDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TemplateFieldInfo {
        private String key;
        private String name;
        private String dataType;
        private boolean isRequired;
        private String sampleValue;
    }
}
