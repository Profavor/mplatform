package com.classification.domain_system.dto;

import lombok.Data;

import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class CodeExportDto extends CodeGroupRequest {
    private List<CodeDetailRequest> details;
}
