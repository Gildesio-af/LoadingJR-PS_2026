package com.loandingjr.chat.dto.chat;

import com.loandingjr.chat.dto.message.MessageResponseDTO;
import com.loandingjr.chat.model.enums.ChatStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

@Schema(description = "Data Transfer Object representing a chat session details")
@Builder
public record ChatResponseDTO(
        @Schema(description = "Unique identifier of the chat")
        String id,
        @Schema(description = "Current status of the chat (e.g., OPEN, ACCEPTED, CLOSED)")
        ChatStatus status,
        @Schema(description = "Username of the user who initiated the chat")
        String initiatorUsername,
        @Schema(description = "Username of the participant invited to the chat")
        String participantUsername,
        @Schema(description = "Paginated list of messages in the chat conversation")
        Page<MessageResponseDTO> messages,
        @Schema(description = "AI-generated summary or report of the conversation")
        String aiReport,
        @Schema(description = "Timestamp when the chat was created")
        LocalDateTime createdAt,
        @Schema(description = "Timestamp when the chat was closed")
        LocalDateTime closedAt
) {
}
