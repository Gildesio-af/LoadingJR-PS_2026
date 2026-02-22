package com.loandingjr.chat.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

@Builder
public record UserCreateDTO(
        @NotBlank(message = "Username cannot be blank")
        @Length(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Email should be valid")
        String email,
        @NotBlank(message = "Password cannot be blank")
        @Length(min = 6, message = "Password must be at least 6 characters long")
        String password
) {
}
