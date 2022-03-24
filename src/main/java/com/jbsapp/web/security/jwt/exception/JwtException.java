package com.jbsapp.web.security.jwt.exception;

import com.jbsapp.web.common.exception.WebException;

public class JwtException extends WebException {

    public JwtException(String msg) {
        super(msg);
    }

}
