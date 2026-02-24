package com.loandingjr.chat.shared.exception;

import org.springframework.http.HttpStatus;

public class UserAlreadyIsInChatException extends ApiException {
    public UserAlreadyIsInChatException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
