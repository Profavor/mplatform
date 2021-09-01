package com.favorsoft.mplatform.exception;

import lombok.Getter;

@Getter
public class MplatformRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1759467431359299110L;

	private final MplatformErrorCode mplatformErrorCode;

	public MplatformRuntimeException(Throwable cause, MplatformErrorCode errorCode) {
		super(cause);
		this.mplatformErrorCode = errorCode;
	}

	public MplatformRuntimeException(MplatformErrorCode errorCode, String message) {
		super(message);
		this.mplatformErrorCode = errorCode;
	}

	public MplatformRuntimeException(Throwable cause, MplatformErrorCode errorCode, String message) {
		// TODO Auto-generated constructor stub
		super(message, cause);
		this.mplatformErrorCode = errorCode;
	}

	public String getErrorCode() {
		return this.mplatformErrorCode.getErrorCode();
	}
}
