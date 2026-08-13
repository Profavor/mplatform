package com.classification.domain_system.entity.enums;

/**
 * 마스터 데이터 레코드의 상태를 나타내는 열거형.
 */
public enum RecordStatus {
    ACTIVE,
    PENDING_APPROVAL,
    REJECTED,
    MERGED,
    DRAFT;
}
