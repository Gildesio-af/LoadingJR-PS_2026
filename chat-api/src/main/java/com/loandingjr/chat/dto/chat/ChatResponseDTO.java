package com.loandingjr.chat.dto.chat;

import com.loandingjr.chat.dto.message.MessageResponseDTO;
import com.loandingjr.chat.model.enums.ChatStatus;
import lombok.Builder;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;

@Builder
public record ChatResponseDTO(
        String id,
        ChatStatus status,
        String initiatorUsername,
        String participantUsername,
        Page<MessageResponseDTO> messages,
        String aiReport,
        LocalDateTime createdAt,
        LocalDateTime closedAt

) {
}
