package com.loandingjr.chat.dto.chat;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record ChatRequestDTO(
        @NotBlank(message = "Participant ID cannot be blank")
        String participantId
) {
}
