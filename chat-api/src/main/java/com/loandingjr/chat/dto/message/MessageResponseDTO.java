package com.loandingjr.chat.dto.message;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MessageResponseDTO(
        String id,
        String senderId,
        String content,
        LocalDateTime sentAt
) {
}
