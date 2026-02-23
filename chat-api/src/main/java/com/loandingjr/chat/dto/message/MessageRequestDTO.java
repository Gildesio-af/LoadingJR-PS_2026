package com.loandingjr.chat.dto.message;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record MessageRequestDTO (
        @NotBlank(message = "Sender ID cannot be blank")
        String content
) {
}
