package com.jbsapp.web.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jbsapp.web.common.model.CommonResponse;
import com.jbsapp.web.common.model.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class WebResponseEntity {

    private static final ObjectMapper objectMapper = new ObjectMapper();

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

    public static void OKFromFilter(HttpServletResponse servletResponse, Object input) throws IOException {
        servletResponse.setContentType("application/json");
        servletResponse.setCharacterEncoding("UTF-8");

        CommonResponse<Object> response = CommonResponse.builder()
                .status(HttpStatus.OK.value())
                .response(input)
                .build();


        objectMapper.writeValue(servletResponse.getWriter(), response);
    }

    public static void ErrorFromFilter(HttpServletResponse servletResponse, HttpStatus httpStatus, String message) throws IOException {
        servletResponse.setContentType("application/json");
        servletResponse.setCharacterEncoding("UTF-8");

        CommonResponse<Object> response = CommonResponse.builder()
                .status(httpStatus.value())
                .error(
                        ErrorResponse.builder().message(message).build()
                )
                .build();

        log.error("Error Message : {}", message);

        objectMapper.writeValue(servletResponse.getWriter(), response);
    }

}
