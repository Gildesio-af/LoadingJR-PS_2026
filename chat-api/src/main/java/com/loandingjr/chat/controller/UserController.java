package com.loandingjr.chat.controller;

import com.loandingjr.chat.dto.user.UserCreateDTO;
import com.loandingjr.chat.dto.user.UserResponseDTO;
import com.loandingjr.chat.dto.user.UserUpdateDTO;
import com.loandingjr.chat.security.CustomUserDetails;
import com.loandingjr.chat.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Objects;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable String id) {
        UserResponseDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/username")
    public ResponseEntity<Page<UserResponseDTO>> getUsersByUsername(@RequestParam(required = false) String username,
                                                                    @PageableDefault(direction = Sort.Direction.ASC) Pageable pageable) {
        return  ResponseEntity.ok(userService.getUsersByUsername(username, pageable));
    }

    @GetMapping("/email")
    public ResponseEntity<UserResponseDTO> getUserByEmail(@RequestParam String email) {
        UserResponseDTO user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserCreateDTO createDTO) {
        UserResponseDTO user = userService.createUser(createDTO);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(user.id())
                .toUri();
        return ResponseEntity.created(location).body(user);
    }

    @PatchMapping
    public ResponseEntity<UserResponseDTO> updateUser(@Valid @RequestBody UserUpdateDTO updateDTO, @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserResponseDTO updatedUser = userService.updateUser(userDetails.getId(), updateDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
