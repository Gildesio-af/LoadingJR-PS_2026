package com.loandingjr.chat.shared.exception;

import org.springframework.http.HttpStatus;

public class ChatStatusException extends ApiException {
    public ChatStatusException(String message) {
        super(message, HttpStatus.CONFLICT);
    }
}
