package com.favorsoft.mplatform.exception;

import lombok.Getter;

@Getter
public enum HttpStatusCode {
	SUCCESS(200),
	BAD_REQUEST(400),
	UNAUTHORIZED(401),
	FORBIDDEN(403),
	NOT_FOUND(404),
	METHOD_NOT_ALLOWED(405),
	INTERNAL_SERVER_ERROR(500);

	private int statusCode;
	HttpStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
}
