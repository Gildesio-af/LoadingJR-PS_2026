package com.loandingjr.chat.shared.exception;

public class InvalidAuthenticationException extends ApiException {
    public InvalidAuthenticationException(String message) {
        super(message, org.springframework.http.HttpStatus.UNAUTHORIZED);
    }
}
