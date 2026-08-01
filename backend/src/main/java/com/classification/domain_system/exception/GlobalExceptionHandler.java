package com.classification.domain_system.exception;

import com.classification.domain_system.service.ErrorLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.PrintWriter;
import java.io.StringWriter;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @Autowired(required = false)
    private ErrorLogService errorLogService;

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        log.warn("Business exception occurred at URI: {}. Code: {}, Message: {}", 
                request.getRequestURI(), ex.getErrorCode().getCode(), ex.getMessage());
        return ResponseEntity
                .status(ex.getErrorCode().getStatus())
                .body(ErrorResponse.of(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found exception at URI: {}. Message: {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(org.springframework.web.servlet.resource.NoResourceFoundException ex, HttpServletRequest request) {
        log.warn("No static resource found at URI: {}", request.getRequestURI());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(ErrorCode.RESOURCE_NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."));
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(org.springframework.http.converter.HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.error("JSON parse error at URI: {}. Message: {}", request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(ErrorCode.INVALID_INPUT, "JSON Parse Error: " + ex.getMessage()));
    }

    @ExceptionHandler({ValidationException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleValidationException(Exception ex, HttpServletRequest request) {
        log.warn("Validation/IllegalArgument exception at URI: {}. Message: {}", request.getRequestURI(), ex.getMessage());
        ErrorCode code = (ex instanceof BusinessException be) ? be.getErrorCode() : ErrorCode.INVALID_INPUT;
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of(code, ex.getMessage()));
    }

    @ExceptionHandler({AccessDeniedException.class, CustomAccessDeniedException.class})
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(Exception ex, HttpServletRequest request) {
        log.warn("Access denied exception at URI: {}. Message: {}", request.getRequestURI(), ex.getMessage());
        ErrorCode code = (ex instanceof BusinessException be) ? be.getErrorCode() : ErrorCode.ACCESS_DENIED;
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of(code, ex.getMessage()));
    }

    @ExceptionHandler({jakarta.persistence.OptimisticLockException.class, org.springframework.orm.ObjectOptimisticLockingFailureException.class})
    public ResponseEntity<ErrorResponse> handleOptimisticLockException(Exception ex, HttpServletRequest request) {
        log.warn("Optimistic lock conflict at URI: {}. Message: {}", request.getRequestURI(), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ErrorResponse.of(ErrorCode.OPTIMISTIC_LOCK_ERROR, "다른 사용자가 이미 이 레코드를 수정했습니다. 새로고침 후 다시 시도해주세요."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex, HttpServletRequest request) {
        String stackTrace = getStackTraceAsString(ex);

        String userId = "anonymous";
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            userId = auth.getName();
        }

        String requestUri = request.getRequestURI();
        String errorMessage = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getName();

        try {
            if (errorLogService != null) {
                errorLogService.logError(userId, requestUri, errorMessage, stackTrace);
            }
        } catch (Exception e) {
            log.error("Failed to save error log to DB", e);
        }

        log.error("Unhandled Exception occurred at URI: {} for User: {}. Message: {}", requestUri, userId, errorMessage, ex);

        // Do not expose internal system details in HTTP 500 response
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR, "내부 오류가 발생했습니다."));
    }

    private String getStackTraceAsString(Throwable throwable) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : throwable.getStackTrace()) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}
