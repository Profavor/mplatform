package com.classification.domain_system.entity.enums;

/**
 * 결재 대상 유형을 나타내는 열거형.
 * 레코드 생성/수정/삭제/병합, 배치, 스키마 변경 등의 대상 타입을 정의합니다.
 */
public enum ApprovalTargetType {
    RECORD,
    RECORD_UPDATE,
    RECORD_DELETE,
    RECORD_MERGE,
    BATCH_RECORD,
    SCHEMA_FIELD_ADD,
    SCHEMA_FIELD_UPDATE, 
    SCHEMA_FIELD_DELETE,
    SCHEMA_CHANGE,
    MEMO;

    /**
     * 스키마 관련 타입인지 확인합니다.
     */
    public boolean isSchemaType() {
        return this == SCHEMA_FIELD_ADD || this == SCHEMA_FIELD_UPDATE 
            || this == SCHEMA_FIELD_DELETE || this == SCHEMA_CHANGE;
    }

    /**
     * 레코드 관련 타입인지 확인합니다.
     */
    public boolean isRecordType() {
        return this == RECORD || this == RECORD_UPDATE 
            || this == RECORD_DELETE || this == RECORD_MERGE;
    }
}
