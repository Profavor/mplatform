package com.favorsoft.mplatform.exception;

import lombok.Getter;

@Getter
public enum MplatformErrorCode {

	// 인증 관련 오류
    /** 인증 실패 오류 */
    AUTH_ERROR(HttpStatusCode.UNAUTHORIZED.getStatusCode(), "1000000"),
    /** 인증 서비스 오류 */
    AUTH_SERVICE_ERROR(HttpStatusCode.UNAUTHORIZED.getStatusCode(), "1000001"),
    /** 계정 미등록 오류 */
    USER_NOT_FOUND(HttpStatusCode.UNAUTHORIZED.getStatusCode(), "1000002"),
    /** 패스워드 입력 오류 */
    INVALID_PASSWORD(HttpStatusCode.UNAUTHORIZED.getStatusCode(), "1000003"),
    /** 세션만료 오류 */
    EXPIRED_SESSION(HttpStatusCode.UNAUTHORIZED.getStatusCode(), "1000004"),

    // 서블릿 요청 관련 오류
    /** REST API 파라미터 유효성 오류 */
    INVALID_REQUEST_PARAM(HttpStatusCode.BAD_REQUEST.getStatusCode(), "2000000"),
    /** 지원하지 않은 요청 데이터 */
    UNSUPPORTED_REQUEST_DATA(HttpStatusCode.BAD_REQUEST.getStatusCode(), "2000001"),
    /** 리소스 미 발견 오류 */
    RESOURCE_NOT_FOUND(HttpStatusCode.NOT_FOUND.getStatusCode(), "2000002"),

    // 데이터베이스 관련 오류
    /** 데이터베이스 쿼리 실행 오류 */
    DATA_ACCESS_ERROR(HttpStatusCode.BAD_REQUEST.getStatusCode(), "3000000"),
    /** 데이터 키 중복 오류 */
    DUPLICATE_KEY_ERROR(HttpStatusCode.BAD_REQUEST.getStatusCode(), "3000001"),
    /** 참조 키 미삭제 오류 */
    FOREIGN_KEY_CONSTRAINT_ERROR(HttpStatusCode.BAD_REQUEST.getStatusCode(), "3000002"),
    /** 잘못된 쿼리 문법 오류 */
    SQL_SYNTAX_ERROR(HttpStatusCode.BAD_REQUEST.getStatusCode(), "3000003"),
    /** 데이터 제약조건 위배 오류 */
    DATA_INTEGRITY_VIOLATION_ERROR(HttpStatusCode.BAD_REQUEST.getStatusCode(), "3000004"),

	// Runtime Error
    INTERNAL_SERVER_ERROR(HttpStatusCode.INTERNAL_SERVER_ERROR.getStatusCode(), "4000000");

	 /** Http 상태 코드 */
    private int httpStatusCode;
    /** 시스템에서 정의하는 에러 코드 */
    private String errorCode;

    MplatformErrorCode(int httpStatusCode, String errorCode) {
        this.httpStatusCode = httpStatusCode;
        this.errorCode = errorCode;
    }
}
