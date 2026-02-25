package com.loandingjr.chat.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

@Schema(description = "Data Transfer Object for creating a new user")
@Builder
public record UserCreateDTO(
        @Schema(description = "Username for the new user. Must be unique and between 3 and 50 characters.", example = "john_doe")
        @NotBlank(message = "Username cannot be blank")
        @Length(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,
        @Schema(description = "Email address for the new user. Must be a valid email format.", example = "john.doe@example.com")
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Email should be valid")
        String email,
        @Schema(description = "Password for the new user. Must be at least 6 characters long.", example = "secretPassword123", accessMode = Schema.AccessMode.WRITE_ONLY)
        @NotBlank(message = "Password cannot be blank")
        @Length(min = 6, message = "Password must be at least 6 characters long")
        String password
) {
}
