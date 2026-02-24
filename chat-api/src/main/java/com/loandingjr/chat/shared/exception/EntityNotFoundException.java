package com.loandingjr.chat.shared.exception;

import org.springframework.http.HttpStatus;

public class EntityNotFoundException extends ApiException {
    public EntityNotFoundException(String entityName, String identifier) {
        super(String.format("%s with identifier %s not found", entityName, identifier), HttpStatus.NOT_FOUND);
    }

    public EntityNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
