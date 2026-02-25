package com.loandingjr.chat.controller;

import com.loandingjr.chat.dto.user.UserCreateDTO;
import com.loandingjr.chat.dto.user.UserResponseDTO;
import com.loandingjr.chat.dto.user.UserUpdateDTO;
import com.loandingjr.chat.security.CustomUserDetails;
import com.loandingjr.chat.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User Controller", description = "Controller responsible for user management operations, including registration, retrieval, updating, and deletion.")
public class UserController {
    private final UserService userService;

    @Operation(summary = "Get User by ID", description = "Retrieves user details by their unique identifier.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(
            @Parameter(description = "ID of the user to be retrieved") @PathVariable String id) {
        UserResponseDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Get Users by Username", description = "Retrieves a paginated list of users filtered by username.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved list of users")
    })
    @GetMapping("/username")
    public ResponseEntity<Page<UserResponseDTO>> getUsersByUsername(
            @Parameter(description = "Username filter string") @RequestParam(required = false, defaultValue = "") String username,
            @Parameter(description = "Pagination and sorting information")
            @PageableDefault(direction = Sort.Direction.ASC) Pageable pageable) {
        return  ResponseEntity.ok(userService.getUsersByUsername(username, pageable));
    }

    @Operation(summary = "Get User by Email", description = "Retrieves user details by their email address.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @GetMapping("/email")
    public ResponseEntity<UserResponseDTO> getUserByEmail(
            @Parameter(description = "Email address of the user to be retrieved") @RequestParam String email) {
        UserResponseDTO user = userService.getUserByEmail(email);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Create a new User", description = "Registers a new user in the system.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User created successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data or validation error", content = @Content),
        @ApiResponse(responseCode = "409", description = "User already exists (e.g. email or username taken)", content = @Content)
    })
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserCreateDTO createDTO) {
        UserResponseDTO user = userService.createUser(createDTO);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(user.id())
                .toUri();
        return ResponseEntity.created(location).body(user);
    }

    @Operation(summary = "Update User", description = "Updates details of the currently authenticated user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User updated successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
        @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @PatchMapping
    public ResponseEntity<UserResponseDTO> updateUser(@Valid @RequestBody UserUpdateDTO updateDTO,
                                                      @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserResponseDTO updatedUser = userService.updateUser(userDetails.getId(), updateDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Delete User", description = "Deletes a user by their ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "User deleted successfully"),
        @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "ID of the user to be deleted") @PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
