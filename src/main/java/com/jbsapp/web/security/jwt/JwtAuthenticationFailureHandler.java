package com.jbsapp.web.security.jwt;

import com.jbsapp.web.common.util.WebResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        WebResponseEntity.ErrorFromFilter(response, HttpStatus.UNAUTHORIZED, "유저 인증에 실패했습니다.");
    }

}
