package com.loandingjr.chat.dto.error;

public record ErrorResponse(
        String message,
        String field
) {
}
