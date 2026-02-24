package com.loandingjr.chat.service;

import com.loandingjr.chat.dto.user.UserCreateDTO;
import com.loandingjr.chat.dto.user.UserResponseDTO;
import com.loandingjr.chat.dto.user.UserUpdateDTO;
import com.loandingjr.chat.model.User;
import com.loandingjr.chat.repository.UserRepository;
import com.loandingjr.chat.shared.exception.EntityNotFoundException;
import com.loandingjr.chat.shared.utils.UserConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    public UserResponseDTO getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User", id));
        return UserConverter.modelToResponse(user);
    }

    public Page<UserResponseDTO> getUsersByUsername(String username, Pageable pageable) {
        return userRepository.findAllByUsernameContainingIgnoreCase(username, pageable)
                .map(UserConverter::modelToResponse);
    }

    public UserResponseDTO getUserByEmail(String email) {
        User user = userRepository.findByEmailContainingIgnoreCase(email)
                .orElseThrow(() -> new EntityNotFoundException("User with email: " + email));
        return UserConverter.modelToResponse(user);
    }

    @Transactional
    public UserResponseDTO createUser(UserCreateDTO createDTO) {
        User user = UserConverter.createDtoToModel(createDTO);
        user.setPassword(encoder.encode(createDTO.password()));
        userRepository.save(user);
        return UserConverter.modelToResponse(user);
    }

    @Transactional
    public UserResponseDTO updateUser(String id, UserUpdateDTO updateDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User", id));
        UserConverter.updateUserFromDto(user, updateDTO);
        if (updateDTO.password() != null) {
            user.setPassword(encoder.encode(updateDTO.password()));
        }
        userRepository.save(user);
        return UserConverter.modelToResponse(user);
    }

    @Transactional
    public void deleteUser(String id) {
        int rowsAffected = userRepository.deactivateUserById(id);

        if (rowsAffected == 0) throw new EntityNotFoundException("User", id);
    }
}
