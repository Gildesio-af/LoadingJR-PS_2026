package com.loandingjr.chat.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Schema(description = "Data Transfer Object representing user details")
@Builder
public record UserResponseDTO(
        @Schema(description = "Unique identifier of the user", example = "550e8400-e29b-41d4-a716-446655440000")
        String id,
        @Schema(description = "Username of the user", example = "john_doe")
        String username,
        @Schema(description = "Email address of the user", example = "john.doe@example.com")
        String email,
        @Schema(description = "Timestamp when the user account was created")
        LocalDateTime createdAt
) {
}
