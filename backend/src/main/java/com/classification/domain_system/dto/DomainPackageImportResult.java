package com.classification.domain_system.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DomainPackageImportResult {
    private UUID domainId;
    private String domainName;
    private int axisCount;
    private int nodeCount;
    private int fieldCount;
    private int dqRuleCount;
    private int matchingRuleCount;
    private int workflowCount;
    private boolean success;
    private String message;
}
