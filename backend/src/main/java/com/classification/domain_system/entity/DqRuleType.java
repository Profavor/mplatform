package com.classification.domain_system.entity;

public enum DqRuleType {
    NOT_NULL,
    REGEX,
    RANGE,
    LENGTH,
    ENUM,
    DATE_RANGE,
    UNIQUE,
    CROSS_FIELD,
    CUSTOM_SPEL,
    BUSINESS_NO_CHECKSUM,
    CORPORATE_NO_CHECKSUM
}
