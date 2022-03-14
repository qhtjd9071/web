package com.jbsapp.web.common.exception;

import com.jbsapp.web.common.model.CommonResponse;
import com.jbsapp.web.common.model.ErrorResponse;
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
		return handleCommonException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
	}

	@ExceptionHandler(BindingException.class)
	public Object handleBindingException(BindingException e) {
		return handleCommonException(HttpStatus.BAD_REQUEST, e.getMessage());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> handleUnknownException(Exception e) {
		return handleCommonException(HttpStatus.INTERNAL_SERVER_ERROR, "Unknown Exception : " + e.getMessage());
	}

	private ResponseEntity<?> handleCommonException(HttpStatus httpStatus, String message) {
		CommonResponse<Object> response = CommonResponse.builder()
				.status(httpStatus.value())
				.error(
						ErrorResponse.builder().message(message).build()
				)
				.build();
		log.error("Error Message : {}", message);
		return new ResponseEntity<>(response, httpStatus);
	}
}
