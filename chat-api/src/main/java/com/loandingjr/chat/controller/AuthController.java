package com.loandingjr.chat.controller;

import com.loandingjr.chat.dto.security.JwtResponse;
import com.loandingjr.chat.dto.security.LoginRequest;
import com.loandingjr.chat.security.Auth0JwtTokenProvider;
import com.loandingjr.chat.security.CustomUserDetails;
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
public class AuthController {
    private final AuthenticationManager authManager;
    private final Auth0JwtTokenProvider jwtTokenProvider;
//    private final AuthService authService;

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

//    @PostMapping("/recover-password")
//    public ResponseEntity<String> recoverPassword(@Valid @RequestBody EmailRequestDTO emailRequest) {
//        authService.recoverPassword(emailRequest);
//        return ResponseEntity.ok("Password recovery email sent if the email exists in our system.");
//    }
//
//    @PatchMapping("/reset-password")
//    public ResponseEntity<String> resetPassword(@RequestParam String token, @Valid @RequestBody PasswordRequestDTO request) {
//        authService.resetPassword(token, request);
//        return ResponseEntity.ok("Password has been reset successfully.");
//    }
}
