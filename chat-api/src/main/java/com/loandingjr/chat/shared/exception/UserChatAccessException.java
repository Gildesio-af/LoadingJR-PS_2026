package com.loandingjr.chat.shared.exception;

import org.springframework.http.HttpStatus;

public class UserChatAccessException extends ApiException {
    public UserChatAccessException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
