package com.classification.domain_system.entity.enums;

/**
 * 결재 요청 및 결재 단계의 상태를 나타내는 열거형.
 * 기존 String 하드코딩을 대체하여 타입 안전성을 보장합니다.
 */
public enum ApprovalStatus {
    PENDING,
    APPROVED, 
    REJECTED,
    WAITING,
    SUBMITTED,
    DRAFT;
}
