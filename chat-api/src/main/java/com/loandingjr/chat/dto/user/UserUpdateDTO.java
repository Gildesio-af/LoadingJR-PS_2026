package com.loandingjr.chat.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

@Schema(description = "Data Transfer Object for updating user information")
@Builder
public record UserUpdateDTO(
            @Schema(description = "New username. Must be between 3 and 50 characters.", example = "john_doe_updated")
            @Length(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
            String username,
            @Schema(description = "New email address. Must be a valid email format.", example = "john.updated@example.com")
            @Email(message = "Email should be valid")
            String email,
            @Schema(description = "New password. Must be at least 6 characters long.", example = "newSecretPassword456", accessMode = Schema.AccessMode.WRITE_ONLY)
            @Length(min = 6, message = "Password must be at least 6 characters long")
            String password
) {
}
