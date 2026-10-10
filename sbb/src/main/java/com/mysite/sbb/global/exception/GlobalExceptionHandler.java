package com.mysite.sbb.global.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> handleValidateion(MethodArgumentNotValidException e) {
		Map<String, String> errors = new LinkedHashMap<>();
		e.getBindingResult().getFieldErrors().forEach(f -> errors.put(f.getField(), f.getDefaultMessage()));
		return ResponseEntity.badRequest().body(errors);
	}
	
	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<Map<String, String>> handleStatus(ResponseStatusException e) {
		return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", e.getReason() != null ? e.getReason() : ""));
	}
	
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<Map<String, String>> handleConflict(DataIntegrityViolationException e) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "이미 등록된 정보입니다."));
	}
}
