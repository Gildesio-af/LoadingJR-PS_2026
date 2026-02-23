package com.loandingjr.chat.shared.utils;

import com.loandingjr.chat.dto.user.UserCreateDTO;
import com.loandingjr.chat.dto.user.UserResponseDTO;
import com.loandingjr.chat.dto.user.UserUpdateDTO;
import com.loandingjr.chat.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserConverter {
    public static UserResponseDTO modelToResponse(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public static User createDtoToModel(UserCreateDTO createDTO) {
        return User.builder()
                .username(createDTO.username())
                .email(createDTO.email())
                .password(createDTO.password())
                .build();
    }

    public static void updateUserFromDto(User user, UserUpdateDTO updateDTO) {
        if (updateDTO.username() != null) user.setUsername(updateDTO.username());
        if (updateDTO.email() != null) user.setEmail(updateDTO.email());
    }

    public static User idToModel(String userId) {
        return User.builder()
                .id(userId)
                .build();
    }
}
