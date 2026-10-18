package com.mysite.sbb.global.exception;

import java.util.Map;

import org.springframework.http.HttpStatus;

public record ErrorResponse(int status, String message, Map<String, String> fieldErrors) {

	public static ErrorResponse of(HttpStatus status, String message) {
		return new ErrorResponse(status.value(), message, null);
	}

	public static ErrorResponse of(HttpStatus status, String message, Map<String, String> fieldErrors) {
		return new ErrorResponse(status.value(), message, fieldErrors);
	}

}
