package com.jbsapp.web.security.jwt.exception;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.jbsapp.web.common.util.WebResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class JwtExceptionHandler extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (JwtException ex) {
            log.error("JwtException: {}", ex.getMessage());
            WebResponseEntity.ErrorFromFilter(response, HttpStatus.BAD_REQUEST, ex.getMessage());
        } catch (TokenExpiredException ex) {
            log.error("TokenExpiredException: {}", ex.getMessage());
            WebResponseEntity.ErrorFromFilter(response, HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다.");
        } catch (Exception ex) {
            log.error("Exception: {}", "Unknown Exception : " + ex.getMessage());
            WebResponseEntity.ErrorFromFilter(response, HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

}
