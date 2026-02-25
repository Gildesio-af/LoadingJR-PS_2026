package com.loandingjr.chat.shared.exception.handler;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        response.setContentType("application/json;charset=UTF-8");

        int status = HttpServletResponse.SC_UNAUTHORIZED;
        String errorTitle = "Unauthorized";
        String message = authException.getMessage();

        if (authException.getCause() instanceof JWTVerificationException) {
            errorTitle = "Invalid Token";
            message = "Token expired or invalid: " + authException.getCause().getMessage();
        }
        else if (authException instanceof BadCredentialsException) {
            errorTitle = "Bad Credentials";
            message = "Username or password is incorrect.";
        }
        else if (authException instanceof InsufficientAuthenticationException) {
            errorTitle = "Access Denied";
        }

        response.setStatus(status);

        String jsonResponse = String.format(
                "{\"timestamp\": \"%s\", \"status\": %d, \"error\": \"%s\", \"message\": \"%s\", \"path\": \"%s\"}",
                Instant.now(),
                status,
                errorTitle,
                message,
                request.getRequestURI()
        );

        response.getWriter().write(jsonResponse);
    }
}
