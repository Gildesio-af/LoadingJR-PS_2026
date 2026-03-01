package com.loandingjr.chat.service;

import com.loandingjr.chat.dto.user.UserCreateDTO;
import com.loandingjr.chat.dto.user.UserResponseDTO;
import com.loandingjr.chat.dto.user.UserUpdateDTO;
import com.loandingjr.chat.model.User;
import com.loandingjr.chat.repository.UserRepository;
import com.loandingjr.chat.shared.exception.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id("user-1")
                .username("john")
                .email("john@test.com")
                .password("encoded")
                .build();
    }

    @Test
    void getUserById_shouldReturnUser() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        UserResponseDTO response = userService.getUserById(user.getId());

        assertThat(response.id()).isEqualTo(user.getId());
        assertThat(response.username()).isEqualTo(user.getUsername());
    }

    @Test
    void getUserById_shouldThrowWhenNotFound() {
        when(userRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById("missing"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getUsersByUsername_shouldMapUsers() {
        when(userRepository.findAllByUsernameContainingIgnoreCase(eq("jo"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(user)));

        assertThat(userService.getUsersByUsername("jo", Pageable.unpaged()).getContent())
                .extracting(UserResponseDTO::username)
                .contains("john");
    }

    @Test
    void getUserByEmail_shouldReturnUser() {
        when(userRepository.findByEmailContainingIgnoreCase(user.getEmail())).thenReturn(Optional.of(user));

        UserResponseDTO response = userService.getUserByEmail(user.getEmail());

        assertThat(response.email()).isEqualTo(user.getEmail());
    }

    @Test
    void createUser_shouldEncodePassword() {
        UserCreateDTO dto = new UserCreateDTO("john", "john@test.com", "plain");
        when(passwordEncoder.encode("plain")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseDTO response = userService.createUser(dto);

        assertThat(response.username()).isEqualTo(dto.username());
        verify(passwordEncoder).encode("plain");
    }

    @Test
    void updateUser_shouldUpdatePasswordWhenProvided() {
        UserUpdateDTO dto = new UserUpdateDTO("johnny", "johnny@test.com", "newpass");
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newpass")).thenReturn("encoded-new");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseDTO response = userService.updateUser(user.getId(), dto);

        assertThat(response.username()).isEqualTo(dto.username());
        verify(passwordEncoder).encode("newpass");
    }

    @Test
    void deleteUser_shouldThrowWhenNoRowsAffected() {
        when(userRepository.deactivateUserById("missing")).thenReturn(0);

        assertThatThrownBy(() -> userService.deleteUser("missing"))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
