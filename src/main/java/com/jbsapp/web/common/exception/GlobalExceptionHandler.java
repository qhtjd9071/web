package com.jbsapp.web.common.exception;

import com.jbsapp.web.common.model.CommonResponse;
import com.jbsapp.web.common.model.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(WebException.class)
	public ResponseEntity<?> handleWebException(WebException e) {
		CommonResponse<Object> response = CommonResponse.builder()
				.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
				.error(new ErrorResponse(e.getMessage()))
				.build();
		log.error("Error Message : {}", e.getMessage());
		return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public Object handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
		CommonResponse<Object> response = CommonResponse.builder()
				.status(HttpStatus.BAD_REQUEST.value())
				.error(new ErrorResponse(e.getMessage()))
				.build();
		log.error("Error Message : {}", e.getMessage());
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<?> handleUnknownException(Exception e) {
		CommonResponse<Object> response = CommonResponse.builder()
				.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
				.error(new ErrorResponse(e.getMessage()))
				.build();
		log.error("Error Message : {}", "Unknown Exception : " + e.getMessage());
		return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
