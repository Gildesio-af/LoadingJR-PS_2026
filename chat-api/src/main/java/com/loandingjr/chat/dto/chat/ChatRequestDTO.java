package com.loandingjr.chat.dto.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Schema(description = "Data Transfer Object for initiating a chat request")
@Builder
public record ChatRequestDTO(
        @Schema(description = "ID of the user to chat with (the participant)", example = "550e8400-e29b-41d4-a716-446655440000")
        @NotBlank(message = "Participant ID cannot be blank")
        String participantId
) {
}
