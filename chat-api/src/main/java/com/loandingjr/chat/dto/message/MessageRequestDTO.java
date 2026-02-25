package com.loandingjr.chat.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Schema(description = "Data Transfer Object for sending a new message")
@Builder
public record MessageRequestDTO (
        @Schema(description = "Content of the message to be sent", example = "Hello, how are you?")
        @NotBlank(message = "Sender ID cannot be blank")
        String content
) {
}
