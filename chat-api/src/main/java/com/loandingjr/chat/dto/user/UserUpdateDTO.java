package com.loandingjr.chat.dto.user;

import jakarta.validation.constraints.Email;
import lombok.Builder;
import org.hibernate.validator.constraints.Length;

@Builder
public record UserUpdateDTO(
            @Length(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
            String username,
            @Email(message = "Email should be valid")
            String email,
            @Length(min = 6, message = "Password must be at least 6 characters long")
            String password
) {
}
