package com.favorsoft.mplatform.exception;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ErrorResponse {
	private String errorCode;
	private String errorString;

	ErrorResponse(String errorCode, String errorString) {
		this.errorCode = errorCode;
		this.errorString = errorString;
	}
}
