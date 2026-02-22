package com.loandingjr.chat.dto.security;

public record LoginRequest(
        String username,
        String password
) {
}

