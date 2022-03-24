package com.jbsapp.web.common.exception;

import com.jbsapp.web.common.util.WebResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(WebException.class)
	public ResponseEntity<?> handleWebException(WebException e) {
		return WebResponseEntity.Error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
	}

	@ExceptionHandler(BindingException.class)
	public Object handleBindingException(BindingException e) {
		return WebResponseEntity.Error(HttpStatus.BAD_REQUEST, e.getMessage());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> handleUnknownException(Exception e) {
		return WebResponseEntity.Error(HttpStatus.INTERNAL_SERVER_ERROR, "Unknown Exception : " + e.getMessage());
	}

}
