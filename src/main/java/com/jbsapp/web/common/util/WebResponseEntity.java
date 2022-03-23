package com.jbsapp.web.common.util;

import com.jbsapp.web.common.model.CommonResponse;
import com.jbsapp.web.common.model.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Slf4j
public class WebResponseEntity {

    public static ResponseEntity<?> OK(Object input) {
        CommonResponse<Object> response = CommonResponse.builder()
                .status(HttpStatus.OK.value())
                .response(input)
                .build();

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public static ResponseEntity<?> Error(HttpStatus httpStatus, String message) {
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
