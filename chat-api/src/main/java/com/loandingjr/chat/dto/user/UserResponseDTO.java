package com.loandingjr.chat.dto.user;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UserResponseDTO(
        String id,
        String username,
        String email,
        LocalDateTime createdAt
) {
}
