package com.loandingjr.chat.shared.exception.handler;

import com.loandingjr.chat.dto.error.CustomError;
import com.loandingjr.chat.shared.exception.ApiException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Object> handleApiException(ApiException e, WebRequest request){
        CustomError<String> error = new CustomError<>(e.getMessage(),
                request.getDescription(false), e.getStatus());

        return new ResponseEntity<>(error, error.getStatus());
    }
}
