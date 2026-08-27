package com.classification.domain_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecializedDomainProvisionRequest {
    private String category; // CUSTOMER, VENDOR, PRODUCT, MATERIAL, EMPLOYEE, STOCK
    private Map<String, String> name; // Optional override
    private Map<String, String> description; // Optional override
    private String numberingPattern; // Optional override
    private String icon; // Optional override
}
