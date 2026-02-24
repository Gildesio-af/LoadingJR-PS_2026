package com.loandingjr.chat.dto.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
public class CustomError<T> {
    private final T error;
    private final String description;
    private final HttpStatus status;
    @JsonFormat(pattern = "yyyy-MM-dd' 'HH:mm:ss")
    private final LocalDateTime timestamp;

    public CustomError(T error, String description, HttpStatus status) {
        this.error = error;
        this.description = description;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

}
