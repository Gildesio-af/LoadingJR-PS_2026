package com.loandingjr.chat.shared.exception;

import org.springframework.http.HttpStatus;

public class AiResponseException extends ApiException {
    public AiResponseException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
