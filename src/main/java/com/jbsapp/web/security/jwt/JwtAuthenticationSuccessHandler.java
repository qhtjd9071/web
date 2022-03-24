package com.jbsapp.web.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.jbsapp.web.common.util.WebResponseEntity;
import com.jbsapp.web.security.auth.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

public class JwtAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        String tmp = customUserDetails.getAuthorities().toString();
        String roles = tmp.substring(1, tmp.length() - 1);

        String authorization = "Bearer " + JWT.create()
                .withSubject(customUserDetails.getUsername())
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 10))
                .withClaim("username", customUserDetails.getUsername())
                .withClaim("roles", roles)
                .sign(Algorithm.HMAC512("secret"));

        JwtToken jwtToken = new JwtToken(authorization);

        WebResponseEntity.OKFromFilter(response, jwtToken);
    }

}
