package com.favorsoft.mplatform.cdn.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
public class ResponseDTO<T> {
    private HttpStatus statusCode;
    private String message;
    private T object;

    @Builder
    public ResponseDTO(final HttpStatus statusCode, final String message, final T object) {
        this.statusCode = statusCode;
        this.message = message;
        this.object = object;
    }
}
