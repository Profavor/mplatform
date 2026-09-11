package com.classification.domain_system.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private String errorCode;
    private String message;
    private Map<String, Object> details;
    private String existingRecordId;
    private List<String> duplicateRecordIds;

    public ErrorResponse(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(errorCode.getCode(), message);
    }

    public static ErrorResponse of(String errorCode, String message) {
        return new ErrorResponse(errorCode, message);
    }

    public static ErrorResponse of(ErrorCode errorCode, String message, Map<String, Object> details) {
        ErrorResponse res = new ErrorResponse(errorCode.getCode(), message);
        res.setDetails(details);
        if (details != null) {
            if (details.containsKey("existingRecordId")) {
                res.setExistingRecordId((String) details.get("existingRecordId"));
            }
            if (details.containsKey("duplicateRecordIds")) {
                Object dupIds = details.get("duplicateRecordIds");
                if (dupIds instanceof List<?> list) {
                    res.setDuplicateRecordIds(list.stream().map(Object::toString).toList());
                }
            }
        }
        return res;
    }
}
