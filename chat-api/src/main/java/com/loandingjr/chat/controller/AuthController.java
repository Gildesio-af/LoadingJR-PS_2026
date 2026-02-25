package com.loandingjr.chat.controller;

import com.loandingjr.chat.dto.security.JwtResponse;
import com.loandingjr.chat.dto.security.LoginRequest;
import com.loandingjr.chat.security.Auth0JwtTokenProvider;
import com.loandingjr.chat.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user authentication and token generation")
public class AuthController {
    private final AuthenticationManager authManager;
    private final Auth0JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "User Login", description = "Authenticates a user with username and password and returns a valid JWT token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtResponse.class),
                    examples = @ExampleObject(
                            name = "Successful Login Response",
                            description = "Example of a successful login response containing the JWT token and user details",
                            value = """
                                        {
                                            "token": "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJDaGF0X0FQSSIsInN1YiI6InRlc3QyIiwiaWF0IjoxNzcxOTc0OTUzLCJleHAiOjE3NzIwNjEzNTMsInJvbGVzIjpbIlJPTEVfbnVsbCJdLCJpZCI6IjU1MGU4NDAwLWUyOWItNDFkNC1hNzE2LTQ0NjY1NTQ0MDAwMCJ9.XLnxU3tfa5brct2zU_TijQVv6mZDe9-hwq6TYemLdXLfB-l-6sePjoABCd1CsqEpTas-JE-4HwTmSSsyJt0xwg",
                                            "id": "550e8400-e29b-41d4-a716-446655440000",
                                            "username": "test2",
                                            "email": "test2@gmail.com"
                                        }
                                    """
                    ))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials (incorrect username or password)", content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Unauthorized Response",
                            description = "Example of a response when login fails due to invalid credentials",
                            value = """
                                        {
                                            "timestamp": "2026-02-24T23:44:20.913494200Z",
                                            "status": 401,
                                            "error": "Bad Credentials",
                                            "message": "Username or password is incorrect",
                                            "path": "/auth/login"
                                        }
                                    """)
            ))
    })
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.username(),
                        loginRequest.password()
                )
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);

        return ResponseEntity.ok(JwtResponse.builder()
                .token(jwt)
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .build());
    }
}
