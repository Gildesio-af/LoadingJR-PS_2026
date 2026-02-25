package com.loandingjr.chat.dto.message;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Schema(description = "Data Transfer Object representing a message in a chat")
@Builder
public record MessageResponseDTO(
        @Schema(description = "Unique identifier of the message")
        String id,
        @Schema(description = "Unique identifier of the message sender")
        String senderId,
        @Schema(description = "Username of the message sender")
        String senderUsername,
        @Schema(description = "Content of the message")
        String content,
        @Schema(description = "Timestamp when the message was sent")
        LocalDateTime sentAt
) {
}
